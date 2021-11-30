@Entity
@Table(name = "sys_department",
        indexes = {@Index(name = "code_uniq",columnList = "code",unique = true)})
public class SysDepartment implements Serializable {

    private static final long serialVersionUID = -8871720562162149293L;

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 10)
    private String code;
    private String remark;
    private Integer status;                        //状态

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable = true,updatable = false)
    private Date cdate;                    //创建时间

    @Transient
    private String statusDesc;

    @Transient
    private List<SysAdminUser> deptUserList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getStatusDesc() {
        if (this.status == null){
            return "无状态";
        } else if (this.status == STATUS_ENABLE) {
            return "可用";
        } else if (this.status == STATUS_DISABLE) {
            return "禁用";
        }
        return "未知";
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<SysAdminUser> getDeptUserList() {
        return deptUserList;
    }

    public void setDeptUserList(List<SysAdminUser> deptUserList) {
        this.deptUserList = deptUserList;
    }
}
