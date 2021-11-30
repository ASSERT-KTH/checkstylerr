
    @RequestMapping("upload")
    @ResponseBody
    public ResponseBean uploadBusinessPic(MultipartFile file){
        try {
            SysAdminUser user = AppSessionHelper.getSessionUser();
            POIExcelHelper excel=new POIExcelHelper();
            List<ArrayList<String>> data= excel.read(file.getInputStream(),false);

            reportInteractionTargetDayService.excelImport(data,user.getId());

        } catch (AppException e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure("导入失败");
        }

        return ResponseBean.buildSuccess();
    }