package com.java110.code;

import java.util.Map;

public class GeneratorDeleteInfoListener extends BaseGenerator {





    /**
     * 生成代码
     * @param data
     */
    public void generator(Data data){
        StringBuffer sb = readFile(this.getClass().getResource("/template/DeleteInfoListener.txt").getFile());
        String fileContext = sb.toString();
        fileContext = fileContext.replace("store",toLowerCaseFirstOne(data.getName()))
                .replace("Store",toUpperCaseFirstOne(data.getName()))
                .replace("商户",data.getDesc())
                .replace("BUSINESS_TYPE_DELETE_STORE_INFO",data.getDeleteBusinessTypeCd())
                .replace(data.getName()+"Id", data.getId())
                .replace(data.getName()+"_id", data.getParams().get(data.getId()).toString());
        System.out.println(this.getClass().getResource("/listener").getPath());
        String writePath = this.getClass().getResource("/listener").getPath()+"/Delete"+toUpperCaseFirstOne(data.getName())+"InfoListener.java";
        writeFile(writePath,
                fileContext);

        //生成协议

        /**
         * |businessstoreMember|memberTypeCd|1|String|30|成员类型|成员类型|
         */
        StringBuffer sbDoc = readFile(this.getClass().getResource("/template/serviceDoc.txt").getFile());
        String fileContextDoc = sbDoc.toString();
        fileContextDoc = fileContextDoc.replace("store",toLowerCaseFirstOne(data.getName()))
                .replace("Store",toUpperCaseFirstOne(data.getName()))
                .replace("商户",data.getDesc())
                .replace("保存","删除")
                .replace("$businessTypeCd$",data.getDeleteBusinessTypeCdValue());

        Map<String,String> tmpParams = data.getParams();
        String tmpLine = "";
        String _tmpLine ="";
            tmpLine += "|business"+toUpperCaseFirstOne(data.getName())+"Info|"+data.getId()+"|1|String|30|-|-|\n";
            _tmpLine += "        \""+data.getId()+"\":\"填写存在的值\",\n";
        _tmpLine = _tmpLine.substring(0,_tmpLine.lastIndexOf(","));
        fileContextDoc = fileContextDoc.replace("$busienssInfo$",tmpLine);
        fileContextDoc = fileContextDoc.replace("$businessInfoJson$",_tmpLine);
        System.out.println(this.getClass().getResource("/listener").getPath());
        String writePathDoc = this.getClass().getResource("/listener").getPath()+"/Delete"+toUpperCaseFirstOne(data.getName())+"Info.md";
        writeFile(writePathDoc,
                fileContextDoc);
    }
}
