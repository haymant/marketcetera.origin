require 'socket'
require 'openssl'
require 'drb/drb'
require 'singleton'

module DRb

  class DRbSSLSocket < DRbTCPSocket

    class SSLConfig

      DEFAULT = {
	:SSLCertificate       => nil,
	:SSLPrivateKey        => nil,
	:SSLClientCA          => nil,
	:SSLCACertificatePath => nil,
	:SSLCACertificateFile => nil,
	:SSLVerifyMode        => ::OpenSSL::SSL::VERIFY_NONE, 
	:SSLVerifyDepth       => nil,
	:SSLVerifyCallback    => nil,   # custom verification
        :SSLCertificateStore  => nil,
	# Must specify if you use auto generated certificate.
	:SSLCertName          => nil,   # e.g. [["CN","fqdn.example.com"]]
	:SSLCertComment       => "Generated by Ruby/OpenSSL"
      }

      def initialize(config)
	@config  = config
        @cert    = config[:SSLCertificate]
        @pkey    = config[:SSLPrivateKey]
        @ssl_ctx = nil
      end

      def [](key); 
	@config[key] || DEFAULT[key]
      end

      def connect(tcp)
	ssl = ::OpenSSL::SSL::SSLSocket.new(tcp, @ssl_ctx)
	ssl.sync = true
	ssl.connect
	ssl
      end
      
      def accept(tcp)
	ssl = OpenSSL::SSL::SSLSocket.new(tcp, @ssl_ctx)
	ssl.sync = true
	ssl.accept
	ssl
      end
      
      def setup_certificate
        if @cert && @pkey
          return
        end

	rsa = OpenSSL::PKey::RSA.new(512){|p, n|
	  next unless self[:verbose]
	  case p
	  when 0; $stderr.putc "."  # BN_generate_prime
	  when 1; $stderr.putc "+"  # BN_generate_prime
	  when 2; $stderr.putc "*"  # searching good prime,
	                            # n = #of try,
                          	    # but also data from BN_generate_prime
	  when 3; $stderr.putc "\n" # found good prime, n==0 - p, n==1 - q,
                         	    # but also data from BN_generate_prime
	  else;   $stderr.putc "*"  # BN_generate_prime
	  end
	}

	cert = OpenSSL::X509::Certificate.new
	cert.version = 3
	cert.serial = 0
	name = OpenSSL::X509::Name.new(self[:SSLCertName])
	cert.subject = name
	cert.issuer = name
	cert.not_before = Time.now
	cert.not_after = Time.now + (365*24*60*60)
	cert.public_key = rsa.public_key
	
	ef = OpenSSL::X509::ExtensionFactory.new(nil,cert)
	cert.extensions = [
	  ef.create_extension("basicConstraints","CA:FALSE"),
	  ef.create_extension("subjectKeyIdentifier", "hash") ]
	ef.issuer_certificate = cert
	cert.add_extension(ef.create_extension("authorityKeyIdentifier",
					       "keyid:always,issuer:always"))
	if comment = self[:SSLCertComment]
	  cert.add_extension(ef.create_extension("nsComment", comment))
	end
	cert.sign(rsa, OpenSSL::Digest::SHA1.new)
	
	@cert = cert
        @pkey = rsa
      end

      def setup_ssl_context
        ctx = ::OpenSSL::SSL::SSLContext.new
        ctx.cert            = @cert
        ctx.key             = @pkey
	ctx.client_ca       = self[:SSLClientCA]
	ctx.ca_path         = self[:SSLCACertificatePath]
	ctx.ca_file         = self[:SSLCACertificateFile]
	ctx.verify_mode     = self[:SSLVerifyMode]
	ctx.verify_depth    = self[:SSLVerifyDepth]
	ctx.verify_callback = self[:SSLVerifyCallback]
        ctx.cert_store      = self[:SSLCertificateStore]
        @ssl_ctx = ctx
      end
    end

    def self.parse_uri(uri)
      if uri =~ /^drbssl:\/\/(.*?):(\d+)(\?(.*))?$/
	host = $1
	port = $2.to_i
	option = $4
	[host, port, option]
      else
	raise(DRbBadScheme, uri) unless uri =~ /^drbssl:/
	raise(DRbBadURI, 'can\'t parse uri:' + uri)
      end
    end

    def self.open(uri, config)
      host, port, option = parse_uri(uri)
      host.untaint
      port.untaint
      soc = TCPSocket.open(host, port)
      ssl_conf = SSLConfig::new(config)
      ssl_conf.setup_ssl_context
      ssl = ssl_conf.connect(soc)
      self.new(uri, ssl, ssl_conf, true)
    end

    def self.open_server(uri, config)
      uri = 'drbssl://:0' unless uri
      host, port, opt = parse_uri(uri)
      if host.size == 0
        host = getservername
        soc = open_server_inaddr_any(host, port)
      else
	soc = TCPServer.open(host, port)
      end
      port = soc.addr[1] if port == 0
      @uri = "drbssl://#{host}:#{port}"
      
      ssl_conf = SSLConfig.new(config)
      ssl_conf.setup_certificate
      ssl_conf.setup_ssl_context
      self.new(@uri, soc, ssl_conf, false)
    end

    def self.uri_option(uri, config)
      host, port, option = parse_uri(uri)
      return "drbssl://#{host}:#{port}", option
    end

    def initialize(uri, soc, config, is_established)
      @ssl = is_established ? soc : nil
      super(uri, soc.to_io, config)
    end
    
    def stream; @ssl; end

    def close
      if @ssl
	@ssl.close
	@ssl = nil
      end
      super
    end
      
    def accept
      begin
      while true
	soc = @socket.accept
	break if (@acl ? @acl.allow_socket?(soc) : true) 
	soc.close
      end
      ssl = @config.accept(soc)
      self.class.new(uri, ssl, @config, true)
      rescue OpenSSL::SSL::SSLError
	warn("#{__FILE__}:#{__LINE__}: warning: #{$!.message} (#{$!.class})") if @config[:verbose]
	retry
      end
    end
  end
  
  DRbProtocol.add_protocol(DRbSSLSocket)
end
