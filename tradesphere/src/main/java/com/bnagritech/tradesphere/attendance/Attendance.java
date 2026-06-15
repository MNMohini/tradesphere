package com.bnagritech.tradesphere.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {
    private String id;
    private String attendanceId;
    private String employeeId;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double checkInLatitude;
    private Double checkOutLatitude;
    private Double checkInLongitude;
    private Double checkOutLongitude;
    private String checkInAddress;
    private String checkOutAddress;
    private String status;
    private Long workingMinutes;
    private Boolean active;
    private LocalDateTime createdBy;
    private LocalDateTime updatedBy;
}

