@Entity
@Table(name = "sys_menu")
public class SysMenu implements Serializable {

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;

    private static final long serialVersionUID = 1463833530703361050L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pid;                       //父级id
    private String name;                   //显示名称
    private String icon;                   //图标
    private String url;                    //跳转地址
    private String prefix;                 //权限前缀
    private Integer sorts;                 //排序
    private Integer status;
    private String domain;                 //菜单域名前缀


    @Transient
    private Boolean checked = false;
    @Transient
    private Boolean parent = false;
    @Transient
    private List<SysMenu> children;

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

    public String getIcon() {
        return icon == null ? "glyphicon glyphicon-map-marker" : icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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

    public List<SysMenu> getChildren() {
        return children;
    }

    public void setChildren(List<SysMenu> children) {
        this.children = children;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
