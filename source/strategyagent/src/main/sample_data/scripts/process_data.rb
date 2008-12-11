#
# author:anshul@marketcetera.com
# since $Release$
# version: $Id$
#
#
'require java'
include_class "org.marketcetera.strategy.ruby.Strategy"

##################################################
# Strategy that processes market data via CEP    #
##################################################
class ProcessData < Strategy
    SYMBOLS = "AMZN,JAVA" # Depends on MD - can be other symbols
    MARKET_DATA_PROVIDER = "marketcetera" # Can also be activ, bogus, opentick
    CEP_QUERY = ["select t.symbol as symbol, t.price * t.size as position from trade t"]
    CEP_PROVIDER = "esper"

    ##########################################
    # Executed when the strategy is started. #
    #                                        #
    # Use this method to set up data flows   #
    #  and other initialization tasks.       #
    ##########################################
    def on_start
      request_processed_market_data(SYMBOLS, MARKET_DATA_PROVIDER, CEP_QUERY.to_java(:string), CEP_PROVIDER)
    end


    ############################################################
    # Executed when the strategy receives data of a type other #
    #  than the other callbacks                                #
    ############################################################
    def on_other(data)
      puts "Trade " + data.to_s
    end
end
