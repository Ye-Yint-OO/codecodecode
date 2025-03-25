package com.prj.LoneHPManagement.Service;

import com.prj.LoneHPManagement.model.dto.UserDTO;
import net.sf.jasperreports.engine.JRException;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface UserReportService {
    ByteArrayOutputStream generateUserPdfReport(Long branchId) throws JRException;
    ByteArrayOutputStream generateUserExcelReport(Long branchId) throws JRException;
    List<UserDTO> getAllUserDTOs(Long branchId);
}
