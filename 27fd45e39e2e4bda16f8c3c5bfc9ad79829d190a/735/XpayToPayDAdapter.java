package design.design_patterns.adapter.PaymentSys;


public class XpayToPayDAdapter implements PayD{

	private String custCardNo;
	private String cardOwnerName;
	private String cardExpMonthDate;
	private Integer cVVNo;
	private Double totalAmount;
	
	private final Xpay xpay;
	
	public XpayToPayDAdapter(Xpay xpay){
		this.xpay = xpay;
		setProp();
	}

	// public void setCustCardNo(String custCardNo);
	// public void setCardOwnerName(String cardOwnerName);
	// public void setCardExpMonthDate(String cardExpMonthDate);
	// public void setCVVNo(Integer cVVNo);
	// public void setTotalAmount(Double totalAmount);

	private void setProp(){

		setCustCardNo(this.xpay.getCreditCardNo());
		setCardOwnerName(this.xpay.getCustomerName());
		setCardExpMonthDate(this.xpay.getCardExpMonth()+"/"+this.xpay.getCardExpYear());
		setCVVNo(this.xpay.getCardCVVNo().intValue());
		setTotalAmount(this.xpay.getAmount());
	}

//	@Override
	public String getCustCardNo() {
		return custCardNo;
	}

//	@Override
	public String getCardOwnerName() {
		return cardOwnerName;
	}

//	@Override
	public String getCardExpMonthDate() {
		return cardExpMonthDate;
	}

//	@Override
	public Integer getCVVNo() {
		return cVVNo;
	}

//	@Override
	public Double getTotalAmount() {
		return totalAmount;
	}

//	@Override
	public void setCustCardNo(String custCardNo) {
		this.custCardNo = custCardNo;
	}

//	@Override
	public void setCardOwnerName(String cardOwnerName) {
		this.cardOwnerName = cardOwnerName;
	}

//	@Override
	public void setCardExpMonthDate(String cardExpMonthDate) {
		this.cardExpMonthDate = cardExpMonthDate;
	}

//	@Override
	public void setCVVNo(Integer cVVNo) {
		this.cVVNo = cVVNo;
	}

//	@Override
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
}
