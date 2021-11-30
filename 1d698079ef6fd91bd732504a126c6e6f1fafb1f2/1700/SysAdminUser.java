@Entity
@Table(name = "sys_admin_user")
public class SysAdminUser implements Serializable {

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;
    public static final int STATUS_WAIT_ACTIVATION = -1;

    private static final long serialVersionUID = -1208346593788772506L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String name;                            //名称
    private String phone;                          //手机号码
    private Integer status;                        //状态
    private Integer isSuper;                      //是否是超级管理员

    private Long cuserId;                         //创建人id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable = true,updatable = false)
    private Date cdate;                    //创建时间




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    public Long getCuserId() {
        return cuserId;
    }

    public void setCuserId(Long cuserId) {
        this.cuserId = cuserId;
    }

    public String getStatusDesc() {
        if (this.status == null){
            return "无状态";
        } else if(this.status == STATUS_WAIT_ACTIVATION){
            return "待激活";
        } else if(this.status == STATUS_ENABLE){
            return "有效";
        } else if (this.status == STATUS_DISABLE) {
            return "禁用";
        } else {
            return "未知";
        }
    }

    public Integer getIsSuper() {
        return isSuper;
    }

    public void setIsSuper(Integer isSuper) {
        this.isSuper = isSuper;
    }
}
