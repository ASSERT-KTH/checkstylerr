import java.util.Date;

public class Form {

    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String address;
    private Date dob;
    private String email;
    private String backupEmail;
    private String spouseName;
    private String city;
    private String state;
    private String country;
    private String language;
    private String passwordHint;
    private String securityQuestion;
    private String securityAnswer;


    protected Form(FormBuilder formBuilder) {

        this.firstName = formBuilder.getFirstName();
        this.lastName = formBuilder.getLastName();
        this.userName = formBuilder.getUserName();
        this.password = formBuilder.getPassword();
        this.address = formBuilder.getAddress();
        this.dob = formBuilder.getDob();
        this.email = formBuilder.getEmail();
        this.backupEmail = formBuilder.getBackupEmail();
        this.spouseName = formBuilder.getSpouseName();
        this.city = formBuilder.getCity();
        this.state = formBuilder.getState();
        this.country = formBuilder.getCountry();
        this.language = formBuilder.getLanguage();
        this.passwordHint = formBuilder.getPasswordHint();
        this.securityQuestion = formBuilder.getSecurityQuestion();
        this.securityAnswer = formBuilder.getSecurityAnswer();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" First Name: ");
        sb.append(firstName);
        sb.append("\\n Last Name: ");
        sb.append(lastName);
        sb.append("\\n User Name: ");
        sb.append(userName);
        sb.append("\\n Password: ");
        sb.append(password);

        if (this.address != null) {
            sb.append("\\n Address: ");
            sb.append(address);
        }
        if (this.dob != null) {
            sb.append("\\n DOB: ");
            sb.append(dob);
        }
        if (this.email != null) {
            sb.append("\\n Email: ");
            sb.append(email);
        }
        if (this.backupEmail != null) {
            sb.append("\\n Backup Email: ");
            sb.append(backupEmail);
        }
        if (this.spouseName != null) {
            sb.append("\\n Spouse Name: ");
            sb.append(spouseName);
        }
        if (this.city != null) {
            sb.append("\\n City: ");
            sb.append(city);
        }
        if (this.state != null) {
            sb.append("\\n State: ");
            sb.append(state);
        }
        if (this.country != null) {
            sb.append("\\n Country: ");
            sb.append(country);
        }
        if (this.language != null) {
            sb.append("\\n Language: ");
            sb.append(language);
        }
        if (this.passwordHint != null) {
            sb.append("\\n Password Hint: ");
            sb.append(passwordHint);
        }
        if (this.securityQuestion != null) {
            sb.append("\\n Security Question: ");
            sb.append(securityQuestion);
        }
        if (this.securityAnswer != null) {
            sb.append("\\n Security Answer: ");
            sb.append(securityAnswer);
        }

        return sb.toString();
    }


//  First Name: Dave\n Last Name: Carter\n User Name: DavCarter\n Password: DAvCaEr123\n City: NY\n Language: English\n Password Hint: MyName
    public static void main(String[] args) {

//        Form form = new Form.FormBuilder("Dave", "Carter", "DavCarter", "DAvCaEr123").passwordHint("MyName").city("NY").language("English").build();

        FormBuilder formBuilder = new FormBuilder("Dave", "Carter", "DavCarter", "DAvCaEr123");
        formBuilder = formBuilder.passwordHint("MyName").city("NY").language("English");

        Form form = formBuilder.build();
        System.out.println(form);
    }
}
