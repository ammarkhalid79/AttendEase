package com.attendease;

import com.attendease.service.AttendanceService.Summary;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceSummaryTest {
    @Test
    void presentAndLateCountTowardAttendance() {
        assertEquals(80.0, new Summary(20, 14, 4, 2).percentage());
    }

    @Test
    void zeroSessionsDisplayNAAndDoNotTriggerAWarning() {
        var summary = new Summary(0, 0, 0, 0);
        assertNull(summary.percentage());
        assertEquals("N/A", summary.display());
        assertFalse(summary.low());
    }

    @Test
    void warningThresholdIsStrictlyBelowSeventyFivePercent() {
        assertTrue(new Summary(4, 1, 2, 1).low());
        assertFalse(new Summary(4, 2, 1, 1).low());
    }
}
