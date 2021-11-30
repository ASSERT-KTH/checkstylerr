package design.design_patterns.adapter.PaymentSys;


/*
* The new vendor, PayD, only allows the PayD type of objects to allow the process
*/
public interface PayD {
	
	public String getCustCardNo();

	public String getCardOwnerName();

	public String getCardExpMonthDate();

	public Integer getCVVNo();

	public Double getTotalAmount();
	



	public void setCustCardNo(String custCardNo);

	public void setCardOwnerName(String cardOwnerName);

	public void setCardExpMonthDate(String cardExpMonthDate);

	public void setCVVNo(Integer cVVNo);

	public void setTotalAmount(Double totalAmount);
}
