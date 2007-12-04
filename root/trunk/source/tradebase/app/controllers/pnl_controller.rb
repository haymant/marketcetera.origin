class PnlController < ApplicationController
  auto_complete_for :m_symbol, :root, {}
  auto_complete_for :account, :nickname, {}
  include ApplicationHelper
  
  def index
    # display the index page   
  end

  # entry point into the search
  def report
    begin
      nickname_str = get_non_empty_string_from_two(params, :account, :nickname, "nickname")
      suffix = (params[:suffix].nil?) ? '' : params[:suffix]
      @report = ReportWithToFromDates.new(params, suffix)
      if(!@report.valid?)
        render :action => :index
        return
      end
      @from_date = @report.from_date.as_date
      @to_date = @report.to_date.as_date
      missing_marks = ProfitAndLoss.get_missing_equity_marks(@from_date)
      missing_marks = missing_marks + ProfitAndLoss.get_missing_equity_marks(@to_date)
      if (missing_marks.length > 0)
        @missing_mark_pages, @missing_marks = paginate_collection(missing_marks, params)
        flash[:error] = "Unable to calculate P&L because some marks are missing."
        render :template => 'pnl/missing_marks'
      else
        if(nickname_str.blank?)
          aggregate
        else
          by_account(nickname_str, params, suffix)
        end
      end
    rescue Exception => ex
      error_string = "Error generating PnL"+(" for #{nickname_str}" unless nickname_str.blank?)+": " + ex
      logger.debug(error_string);
      logger.debug(ex.backtrace)
      flash.now[:error] = error_string
    end
  end

  private
  def by_account(nickname_str, params, suffix)
    @report = PnlByAccount.new(nickname_str, params, suffix)
    if(!@report.valid?)
      render :action => :index
      return
    end
    theAcct = @report.account
            
    pnls = ProfitAndLoss.get_equity_pnl_detail(theAcct, @from_date, @to_date)
    @nickname = theAcct.nickname
    @pnl_pages, @pnls = paginate_collection(pnls, params)
    render :template => 'pnl/pnl_by_account'
  end

  def aggregate
      pnls = ProfitAndLoss.get_equity_pnl(@from_date, @to_date)
      pnls = pnls + ProfitAndLoss.get_forex_pnl(@from_date, @to_date)

      @pnl_pages, @pnls = paginate_collection(pnls, params)
      render :template => 'pnl/pnl_aggregate'
  end

end
