package com.techrentalhub.admin.kpi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kpi")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class StaffKpiController {

    private final StaffKpiService staffKpiService;

    @GetMapping("/staff")
    public ResponseEntity<List<StaffKpiResponse>> getAllStaffKpi() {
        return ResponseEntity.ok(staffKpiService.getAllStaffKpi());
    }

    @GetMapping("/me")
    public ResponseEntity<StaffKpiResponse> getMyKpi(Authentication authentication) {
        return ResponseEntity.ok(staffKpiService.getKpiByAdminEmail(authentication.getName()));
    }

    @GetMapping("/staff/{email}")
    public ResponseEntity<StaffKpiResponse> getAdminKpi(@PathVariable String email) {
        return ResponseEntity.ok(staffKpiService.getKpiByAdminEmail(email));
    }
}
