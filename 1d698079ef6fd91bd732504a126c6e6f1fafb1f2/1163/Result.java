package com.hlj.proj.service.flow.base.entity;


/**
 * @ClassName Result
 * @Author TD
 * @Date 2019/6/12 17:52
 * @Description 流程结果
 */
public class Result<T> {

    public StatusEnum status;

    public T data;


    public enum StatusEnum {
        Success("10", "成功"),
        Fail("99", "失败"),
        Suspend("20", "待处理");
        private String code;
        private String desc;

        StatusEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public enum AuditStatusEnum {
        Success("10", "成功"),
        Suspend("20", "待审批"),
        Reject("90", "拒绝"),
        UNKNOWN("99", "未知"),

            ;
        private String code;
        private String desc;
        AuditStatusEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }




    public static <T> Result<T> success(T data) {
        Result<T> result = new Result();
        result.data = data;
        result.status = StatusEnum.Success;
        return result;
    }

    public static <T> Result<T> fail(T data) {
        Result<T> result = new Result();
        result.data = data;
        result.status = StatusEnum.Fail;
        return result;
    }

    public static <T> Result<T> suspend(T data) {
        Result<T> result = new Result();
        result.data = data;
        result.status = StatusEnum.Suspend;
        return result;
    }
}
