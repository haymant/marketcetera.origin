# Report for server info page in diagnostics controller
# Report for trying to upload marks
# The incoming object is a StringIO obj that we get
# as the representation of the underlying file data the browser sends us.
class ServerInfo < Tableless

  # takes an incoming params hash
  def initialize(eth0, time_diff, host_resolution, process_info)
    @eth0 = eth0
    @time_diff = time_diff
    @host_resolution  = host_resolution
    @process_info = process_info
  end

  def validate
    errors.add("Server clock", "is off") unless @time_diff <= 30
    errors.add("Interface eth0", "is not configured") unless @eth0 != ""
    @host_resolution.each_key {|key|
      errors.add("Host [#{key}]", "does not resolve") unless @host_resolution[key]
    }
    @process_info.each_key {|key|
      errors.add("Process [#{key}]", "is not running") unless !@process_info[key].blank? 
    }
  end
end
