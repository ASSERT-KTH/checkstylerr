@Entity
@Table(name = "sys_auth_role")
public class SysAuthRole implements Serializable {

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;

    private static final long serialVersionUID = 7857249145879679710L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pid;                      //父级id
    private String code;                   //code
    private String name;                   //显示名称
    private Integer sorts;                 //排序
    private Integer status;
    private Integer dataType;              //数据类型  1 查看所有  0 非查看所有
    private Integer rank;                  //角色的层级 用不上


    @Transient
    private Boolean checked = false;
    @Transient
    private Boolean parent = false;
    @Transient
    private String statusDesc;
    @Transient
    private String menuIds;
    @Transient
    private String dataTypeDesc;
    @Transient
    private List<SysAuthRole> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSorts() {
        return sorts;
    }

    public void setSorts(Integer sorts) {
        this.sorts = sorts;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Boolean getParent() {
        return parent;
    }

    public void setParent(Boolean parent) {
        this.parent = parent;
    }

    public List<SysAuthRole> getChildren() {
        return children;
    }

    public void setChildren(List<SysAuthRole> children) {
        this.children = children;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getStatusDesc() {
        if (this.status == null){
            return "";
        }
        if (this.status.compareTo(SysAuthRole.STATUS_ENABLE) == 0){
            return "可用";
        } else if (this.status.compareTo(SysAuthRole.STATUS_DISABLE) == 0){
            return "禁用";
        }
        return "";
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getDataTypeDesc() {
        if (this.dataType == null){
            return "";
        }
        if (this.dataType == 1){
            return "查看所有";
        } else if (this.dataType == 0){
            return "继承权限";
        }
        return "";
    }

    public void setDataTypeDesc(String dataTypeDesc) {
        this.dataTypeDesc = dataTypeDesc;
    }

    public String getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(String menuIds) {
        this.menuIds = menuIds;
    }
}
