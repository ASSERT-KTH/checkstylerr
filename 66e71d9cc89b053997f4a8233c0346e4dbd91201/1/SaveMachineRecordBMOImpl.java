/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.common.bmo.machineRecord.impl;

import com.java110.common.bmo.machineRecord.ISaveMachineRecordBMO;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.file.FileDto;
import com.java110.dto.machine.MachineRecordDto;
import com.java110.intf.common.IFileInnerServiceSMO;
import com.java110.intf.common.IFileRelInnerServiceSMO;
import com.java110.intf.common.IMachineRecordInnerServiceSMO;
import com.java110.po.file.FileRelPo;
import com.java110.po.machine.MachineRecordPo;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存 开门记录
 *
 * @desc add by 吴学文 17:37
 */
@Service
public class SaveMachineRecordBMOImpl implements ISaveMachineRecordBMO {

    @Autowired
    private IFileInnerServiceSMO fileInnerServiceSMOImpl;

    @Autowired
    private IFileRelInnerServiceSMO fileRelInnerServiceSMOImpl;

    @Autowired
    private IMachineRecordInnerServiceSMO machineRecordInnerServiceSMOImpl;

    @Override
    public ResponseEntity<String> saveRecord(MachineRecordDto machineRecordDto) {
        machineRecordDto.setMachineRecordId(GenerateCodeFactory.CODE_PREFIX_machineRecordId);
        if (StringUtil.isEmpty(machineRecordDto.getPhoto())) {
            FileDto fileDto = new FileDto();
            fileDto.setFileId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_file_id));
            fileDto.setFileName(fileDto.getFileId());
            fileDto.setContext(machineRecordDto.getPhoto());
            fileDto.setSuffix("jpeg");
            fileDto.setCommunityId(machineRecordDto.getCommunityId());
            String fileName = fileInnerServiceSMOImpl.saveFile(fileDto);

            FileRelPo fileRelPo = new FileRelPo();
            fileRelPo.setFileRelId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_fileRelId));
            fileRelPo.setRelTypeCd("60000");
            fileRelPo.setSaveWay("table");
            fileRelPo.setObjId(machineRecordDto.getMachineRecordId());
            fileRelPo.setFileRealName(fileName);
            fileRelPo.setFileSaveName(fileName);
            fileRelInnerServiceSMOImpl.saveFileRel(fileRelPo);
        }

        List<MachineRecordPo> machineRecordPos = new ArrayList<>();
        MachineRecordPo machineRecordPo = BeanConvertUtil.covertBean(machineRecordDto, MachineRecordPo.class);
        machineRecordPos.add(machineRecordPo);

        int count = machineRecordInnerServiceSMOImpl.saveMachineRecords(machineRecordPos);

        if (count > 0) {
            return ResultVo.success();
        }
        return ResultVo.error("上传记录失败");
    }
}
