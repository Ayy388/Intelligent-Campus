package com.campus.module.club.service.impl;

import com.campus.common.BusinessException;
import com.campus.module.admin.service.AdminService;
import com.campus.module.club.entity.Club;
import com.campus.module.club.entity.ClubMember;
import com.campus.module.club.mapper.ClubMapper;
import com.campus.module.club.mapper.ClubMemberMapper;
import com.campus.module.club.mapper.VenueBookingMapper;
import com.campus.module.club.mapper.VenueMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceImplTest {

    @Mock
    private ClubMapper clubMapper;

    @Mock
    private ClubMemberMapper memberMapper;

    @Mock
    private VenueMapper venueMapper;

    @Mock
    private VenueBookingMapper bookingMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private ClubServiceImpl clubService;

    @Captor
    private ArgumentCaptor<Club> clubCaptor;

    @Captor
    private ArgumentCaptor<ClubMember> memberCaptor;

    private Club sampleClub;

    @BeforeEach
    void setUp() {
        sampleClub = new Club();
        sampleClub.setId(1L);
        sampleClub.setName("篮球社");
        sampleClub.setDescription("篮球爱好者社团");
        sampleClub.setPresidentId(10L);
    }

    // ===== createClub (saveClub) =====

    @Test
    @DisplayName("创建社团成功 - 应设置待审核状态并添加社长成员")
    void testSaveClub_whenValidRequest_shouldInsertClubAndPresident() {
        // Arrange
        when(clubMapper.insert(any(Club.class))).thenAnswer(invocation -> {
            Club c = invocation.getArgument(0);
            c.setId(1L); // simulate auto-generated ID
            return 1;
        });

        // Act
        clubService.saveClub(sampleClub);

        // Assert
        verify(clubMapper, times(1)).insert(clubCaptor.capture());
        Club savedClub = clubCaptor.getValue();
        assertEquals(Integer.valueOf(0), savedClub.getStatus()); // 待审核
        assertEquals(Integer.valueOf(0), savedClub.getMemberCount());
        assertEquals("篮球社", savedClub.getName());

        verify(memberMapper, times(1)).insert(memberCaptor.capture());
        ClubMember president = memberCaptor.getValue();
        assertEquals(savedClub.getId(), president.getClubId());
        assertEquals(Long.valueOf(10L), president.getUserId());
        assertEquals("president", president.getRole());
        assertEquals(Integer.valueOf(0), president.getStatus());
        assertNotNull(president.getApplyTime());
    }

    @Test
    @DisplayName("创建同名社团 - 应仍能成功创建（无去重逻辑）")
    void testSaveClub_whenDuplicateName_shouldStillInsert() {
        // Arrange
        Club club2 = new Club();
        club2.setName("篮球社");
        club2.setPresidentId(11L);

        when(clubMapper.insert(any(Club.class))).thenReturn(1);

        // Act
        clubService.saveClub(sampleClub);
        clubService.saveClub(club2);

        // Assert
        verify(clubMapper, times(2)).insert(any(Club.class));
        verify(memberMapper, times(2)).insert(any(ClubMember.class));
    }

    @Test
    @DisplayName("创建社团无社长 - 应仍能创建但不添加社长成员")
    void testSaveClub_whenNoPresident_shouldInsertWithoutPresidentMember() {
        // Arrange
        sampleClub.setPresidentId(null);
        when(clubMapper.insert(any(Club.class))).thenReturn(1);

        // Act
        clubService.saveClub(sampleClub);

        // Assert
        verify(clubMapper, times(1)).insert(any(Club.class));
        verify(memberMapper, never()).insert(any(ClubMember.class));
    }

    // ===== approveClub =====

    @Test
    @DisplayName("审批通过社团 - 应更新社团状态为通过并激活社长")
    void testApproveClub_whenApproved_shouldActivateClubAndPresident() {
        // Arrange
        Long clubId = 1L;
        Club existing = new Club();
        existing.setId(clubId);
        existing.setName("篮球社");
        existing.setStatus(0);
        existing.setPresidentId(10L);

        ClubMember president = new ClubMember();
        president.setId(100L);
        president.setClubId(clubId);
        president.setUserId(10L);
        president.setRole("president");
        president.setStatus(0);

        when(clubMapper.selectById(clubId)).thenReturn(existing);
        when(memberMapper.selectOne(any())).thenReturn(president);

        // Act
        clubService.approveClub(clubId, 1);

        // Assert
        verify(clubMapper).updateById(clubCaptor.capture());
        assertEquals(Integer.valueOf(1), clubCaptor.getValue().getStatus());

        verify(memberMapper).updateById(memberCaptor.capture());
        assertEquals(Integer.valueOf(1), memberCaptor.getValue().getStatus());
        assertNotNull(memberCaptor.getValue().getApproveTime());
    }

    @Test
    @DisplayName("审批拒绝社团 - 应更新社团状态为拒绝并删除社长成员")
    void testApproveClub_whenRejected_shouldRejectClubAndDeletePresident() {
        // Arrange
        Long clubId = 1L;
        Club existing = new Club();
        existing.setId(clubId);
        existing.setName("篮球社");
        existing.setStatus(0);

        when(clubMapper.selectById(clubId)).thenReturn(existing);
        // memberMapper.delete returns 0 simulated, but that's fine

        // Act
        clubService.approveClub(clubId, 2);

        // Assert
        verify(clubMapper).updateById(clubCaptor.capture());
        assertEquals(Integer.valueOf(2), clubCaptor.getValue().getStatus());

        verify(memberMapper, never()).updateById(any());
        verify(memberMapper).delete(any());
    }

    @Test
    @DisplayName("审批不存在的社团 - 应抛出 BusinessException")
    void testApproveClub_whenClubNotFound_shouldThrowException() {
        when(clubMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> clubService.approveClub(999L, 1));
        assertEquals("社团不存在", ex.getMessage());
    }

    @Test
    @DisplayName("审批社团传入无效状态 - 应抛出 BusinessException")
    void testApproveClub_whenInvalidStatus_shouldThrowException() {
        Club existing = new Club();
        existing.setId(1L);
        existing.setStatus(0);
        when(clubMapper.selectById(1L)).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> clubService.approveClub(1L, 3));
        assertEquals("无效的审核状态", ex.getMessage());
    }

    // ===== getClubs =====

    @Test
    @DisplayName("获取社团列表 - 应返回所有社团")
    void testGetClubs_shouldReturnAllClubs() {
        clubService.getClubs();
        verify(clubMapper).selectList(any());
    }
}