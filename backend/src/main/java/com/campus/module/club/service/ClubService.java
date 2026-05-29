package com.campus.module.club.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.club.entity.*;
import java.util.List;

public interface ClubService {
    List<Club> getClubs();
    Club getClubById(Long id);
    void saveClub(Club c);
    void updateClub(Long id, Club c);
    void approveClub(Long clubId, Integer status);
    ClubMember applyMember(Long clubId, Long userId, String reason);
    void approveMember(Long memberId, Integer status);
    List<ClubMember> getMembers(Long clubId);
    List<ClubMember> getMyMemberships(Long userId);
    void leaveClub(Long clubId, Long userId);
    void disbandClub(Long clubId, Long userId);

    Page<Activity> pageActivities(Long clubId, int page, int size);
    void saveActivity(Activity a);
    ActivityEnrollment enroll(Long activityId, Long userId);
    List<ActivityEnrollment> getEnrollments(Long activityId);
    void updateActivitySummary(Long id, String summary, String images);

    List<Venue> getVenues();
    Page<VenueBooking> pageBookings(Long userId, String role, int page, int size);
    void applyBooking(VenueBooking b);
    void approveBooking(Long id, Long approverId, Integer status, String reason);
    void deleteClub(Long id);
    void deleteActivity(Long id);
    void cancelEnroll(Long id, Long userId);
    void saveVenue(Venue v);
    void updateVenue(Long id, Venue v);
    void deleteVenue(Long id);
}
