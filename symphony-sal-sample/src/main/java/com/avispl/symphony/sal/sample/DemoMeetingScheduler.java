package com.avispl.symphony.sal.sample;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.avispl.symphony.api.sal.SchedulingService;
import com.avispl.symphony.api.sal.dto.CancellationRequest;
import com.avispl.symphony.api.sal.dto.CustomRoomDetails;
import com.avispl.symphony.api.sal.dto.EmailParticipant;
import com.avispl.symphony.api.sal.dto.FavoriteCustomRoomRequest;
import com.avispl.symphony.api.sal.dto.FavoriteRoomRequest;
import com.avispl.symphony.api.sal.dto.Location;
import com.avispl.symphony.api.sal.dto.Meeting;
import com.avispl.symphony.api.sal.dto.MeetingOccurrenceCancellationRequest;
import com.avispl.symphony.api.sal.dto.RoomDetails;
import com.avispl.symphony.api.sal.dto.RoomListResponse;
import com.avispl.symphony.api.sal.dto.RoomScheduledMeeting;

/**
 * Schedules a demo meeting
 */
public class DemoMeetingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DemoMeetingScheduler.class);

    private static final String MEETING_TIMEZONE = "(UTC-05:00) Eastern Time (US & Canada)";
    private static final String MEETING_SETUP = "Automatic";
    private static final String MEETING_PURPOSE = "RegularMeeting";
    private static final String MEETING_SOURCE = "User";
    private static final String RECURRENCE = "RRULE:FREQ=DAILY;INTERVAL=1;COUNT=1";
    private static final String RECURRENT_INSTANCE_ID = "TZID=Europe/Kiev:20181029T040000";
    private static final String PRODUCER_SERVICE = "NoAttendance";
    private static final String CUSTOM_ROOM_NAME = "SAL sample custom room";
    private static final String CUSTOM_ROOM_ADDRESS = "127.0.0.1";
    private static final String CUSTOM_ROOM_PROTOCOL = "H323";

    private UUID accountId;
    private UUID userId;
    private String meetingOwner;
    private String meetingRequester;
    private Long meetingStart;
    private Long meetingEnd;
    private Set<EmailParticipant> participants;

    @Autowired
    private SchedulingService schedulingService;

    /**
     * Schedules, retrieves and cancels a demo meeting
     */
    public void scheduleAndCancelDemoMeeting() {
        try {
            final Long meetingId = scheduleDemoMeeting();

            Meeting scheduledMeeting = retrieveDemoMeeting(meetingId);

            scheduledMeeting = updateMeetingStartAndEnd(scheduledMeeting);

            // as we have updated meeting start value, now we have to use the updated one when we want to retrieve meeting or its single occurrence
            handleDemoMeetingOccurrences(meetingId, scheduledMeeting.getStart());

            handleRooms(accountId, userId);

            handleCustomRooms(userId);

            fetchMeetingsForRooms(accountId, meetingStart, meetingEnd);

            fetchMeetingsForRequester(accountId, meetingRequester, meetingStart, meetingEnd);

            cancelDemoMeeting(meetingId);
        } catch (Exception e) {
            logger.warn("Failed to schedule SAL demo meeting", e);
        }
    }

    public void fetchMeetingsForRooms(UUID accountId, Long startDate, Long endDate) {
        List<UUID> roomIds = fetchRoomIds(accountId);
        UUID[] roomIdsArray = roomIds.toArray(new UUID[roomIds.size()]);
        try {
            Map<UUID, Set<RoomScheduledMeeting>> roomToMeetingsMap =
                    schedulingService.listMeetingRoomSchedules(roomIdsArray, startDate, endDate);
            logger.info("Found scheduled meetings for rooms {}", roomToMeetingsMap);
        } catch (Exception e) {
            logger.warn("Failed to fetch meeting scheduled for rooms {}", roomIds, e);
        }
    }

    /**
     * Handles recurrent meeting occurrences.
     * Note that it's possible only if we schedule recurrent ({@link #RECURRENCE}) meeting
     */
    public void handleDemoMeetingOccurrences(Long meetingId, Long meetingStart) {
        retrieveMeetingOccurrence(meetingId, meetingStart, RECURRENT_INSTANCE_ID);

        Long recurrentMeetingStart = meetingStart;

        Meeting meetingOccurrence = retrieveMeetingOccurrence(meetingId, recurrentMeetingStart, RECURRENT_INSTANCE_ID);

        updateMeetingOccurrence(recurrentMeetingStart, meetingOccurrence);

        cancelMeetingOccurrence(meetingId, recurrentMeetingStart, RECURRENT_INSTANCE_ID);

        retrieveMeetingOccurrence(meetingId, recurrentMeetingStart, RECURRENT_INSTANCE_ID);
    }

    public void handleRooms(UUID accountId, UUID userId) {
        try {
            List<UUID> roomIds = fetchRoomIds(accountId);
            if (roomIds.isEmpty()) {
                throw new RuntimeException("No rooms found");
            }

            final UUID roomId = roomIds.get(RandomUtils.nextInt(roomIds.size())); // choose any fetched room

            addFavoriteRoom(userId, roomId);

            fetchFavoriteRooms(userId);

            removeFavoriteRoom(userId, roomId);

            fetchRecentRooms(userId);
        } catch (Exception e) {
            logger.warn("Failed to handle rooms for account {} and user {}", accountId, userId, e);
        }
    }

    public void handleCustomRooms(UUID userId) {
        addFavoriteCustomRoom(userId);
    }

    public void addFavoriteCustomRoom(UUID userId) {
        logger.info("Adding favorite custom room for user {}", userId);
        FavoriteCustomRoomRequest request = new FavoriteCustomRoomRequest();
        request.setUserId(userId);
        CustomRoomDetails customRoom = new CustomRoomDetails();
        customRoom.setAddress(CUSTOM_ROOM_ADDRESS);
        customRoom.setName(CUSTOM_ROOM_NAME);
        customRoom.setProtocol(CUSTOM_ROOM_PROTOCOL);
        request.setDetails(customRoom);
        Long customRoomId;
        try {
            // as a response we expect numeric identifier of the persisted custom room (i.e. 42)
            customRoomId = schedulingService.addFavoriteCustomRoom(request);
            logger.info("Custom room has been added with id {}", customRoomId);
        } catch (Exception e) {
            logger.warn("Failed to add favorite custom room for user {}", userId, e);
        }
    }

    public void fetchRecentRooms(UUID userId) {
        logger.info("Fetching recent rooms for user {}", userId);
        try {
            RoomListResponse response = schedulingService.listRecentRooms(userId);
            logger.info("Found recent rooms {} for user {}", response, userId);
        } catch (Exception e) {
            logger.warn("Failed to fetch recent rooms for user {}", userId, e);
        }
    }

    public void fetchFavoriteRooms(UUID userId) {
        logger.info("Fetching favorite rooms for user {}", userId);
        try {
            RoomListResponse response = schedulingService.listFavoriteRooms(userId);
            logger.info("Found favorite rooms {} for user {}", response, userId);
        } catch (Exception e) {
            logger.warn("Failed to fetch favorite rooms for account {}", userId, e);
        }
    }

    public void removeFavoriteRoom(UUID accountId, UUID roomId) {
        logger.info("Removing favorite room {} for account {}", roomId, accountId);
        FavoriteRoomRequest request = new FavoriteRoomRequest();
        request.setUserId(accountId);
        request.setRoomId(roomId);
        try {
            schedulingService.removeFavoriteRoom(request);
        } catch (Exception e) {
            logger.warn("Failed to remove favorite room {} for account {}", roomId, accountId, e);
        }
    }

    public List<UUID> fetchRoomIds(UUID accountId) {
        Set<Location> locations = fetchLocations(accountId);
        UUID locationId = locations.stream()
                .map(Location::getId)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No locations found"));

        Set<RoomDetails> rooms = fetchRooms(accountId, locationId);
        return rooms.stream()
                .map(RoomDetails::getRoomId)
                .collect(Collectors.toList());
    }

    public Set<RoomDetails> fetchRooms(UUID accountId, UUID locationId) {
        logger.info("Fetching rooms by account {} and location {}", accountId, locationId);
        try {
            Set<RoomDetails> rooms = schedulingService.listRooms(accountId, locationId);
            logger.info("Found rooms {}", rooms);
            return rooms;
        } catch (Exception e) {
            logger.warn("Failed to fetch rooms by account {} and location {}", accountId, locationId, e);
        }
        return Collections.emptySet();
    }

    public Set<Location> fetchLocations(UUID userAccountId) {
        logger.info("Fetching locations by user id {}", userAccountId);
        try {
            Set<Location> locations = schedulingService.listLocations(userAccountId);
            logger.info("Found locations {}", locations);
            return locations;
        } catch (Exception e) {
            logger.warn("Failed to fetch locations by user id {}", userAccountId, e);
        }
        return Collections.emptySet();
    }

    public void addFavoriteRoom(UUID userId, UUID roomId) {
        logger.info("Adding favorite room for account {} and room id {}", userId, roomId);
        FavoriteRoomRequest request = new FavoriteRoomRequest();
        request.setUserId(userId);
        request.setRoomId(roomId);
        try {
            schedulingService.addFavoriteRoom(request);
            logger.info("Favorite room has been added");
        } catch (Exception e) {
            logger.warn("Failed to add favorite room for account {} and room {}", userId, roomId, e);
        }
    }

    public Long scheduleDemoMeeting() throws Exception {
        logger.info("Creating SAL demo meeting");

        Meeting meeting = new Meeting();
        meeting.setAccountId(accountId);
        meeting.setEmailParticipants(participants);
        meeting.setScheduledMeetingRooms(Collections.emptySet());
        meeting.setScheduledMeetingCustomRooms(Collections.emptySet());
        meeting.setMeetingSource(MEETING_SOURCE);
        meeting.setMeetingPurpose(MEETING_PURPOSE);
        meeting.setSubject("SAL demo meeting");
        meeting.setNotes("Fake meeting");
        meeting.setJoinByPhone(false);
        meeting.setJoinByWeb(false);
        meeting.setRecorded(false);
        meeting.setVip(true);
        meeting.setSendEmailNotification(true);
        meeting.setRecurrence(RECURRENCE);
        meeting.setRecurrentInstanceId(RECURRENT_INSTANCE_ID);
        meeting.setProducerService(PRODUCER_SERVICE);
        meeting.setOwnerEmail(meetingOwner);
        meeting.setRequestorEmail(meetingRequester);
        meeting.setSetupTimeMinutes(15);
        meeting.setStart(meetingStart);
        meeting.setEnd(meetingEnd);
        meeting.setTimeZone(MEETING_TIMEZONE);
        meeting.setMeetingSetup(MEETING_SETUP);

        Long meetingId = schedulingService.createMeeting(meeting);
        logger.info("SAL demo meeting has been created with id: {}", meetingId);
        return meetingId;
    }

    public Meeting retrieveDemoMeeting(Long meetingId) {
        logger.info("Retrieving meeting of id {}", meetingId);
        Meeting scheduledMeeting = null;
        try {
            scheduledMeeting = schedulingService.retrieveMeeting(meetingId);

            logger.info("Found meeting {}", scheduledMeeting);
        } catch (Exception e) {
            logger.warn("Failed to retrieve meeting of id {}", meetingId, e);
        }
        return scheduledMeeting;
    }

    public void fetchMeetingsForRequester(UUID accountId, String meetingRequester, Long startDate, Long endDate) {
        logger.info("Retrieving meetings requested by {}", meetingRequester);
        try {
            Set<Meeting> meetings = schedulingService.listMeetingsByUsers(accountId, startDate, endDate, meetingRequester);
            logger.info("Found meetings {} requested by {}", meetings, meetingRequester);
        } catch (Exception e) {
            logger.warn("Failed to retrieve meetings requested by {}", meetingRequester, e);
        }
    }

    public Meeting updateMeetingStartAndEnd(Meeting meeting) {
        if (meeting == null) {
            logger.warn("Skipped updating nullable meeting");
            return null;
        }

        logger.info("Updating SAL meeting start and end values");
        long shift = 1000L;
        try {
            meeting.setStart(meeting.getStart() + shift);
            meeting.setEnd(meeting.getEnd() + shift);
            schedulingService.updateMeeting(meeting);
        } catch (Exception e) {
            logger.warn("Failed to update scheduled SAL meeting of id {}", meeting.getMeetingId(), e);
        }
        return meeting;
    }

    /**
     * Here we retrieve single occurrence of the scheduled demo meeting, note that it's possible only because our meeting is recurrent {@link #RECURRENCE}
     *
     * @param recurrentMeetingId     meeting id
     * @param recurrentInstanceStart
     * @param recurrentInstanceId
     */
    public Meeting retrieveMeetingOccurrence(Long recurrentMeetingId, Long recurrentInstanceStart, String recurrentInstanceId) {
        Meeting scheduledRecurrentMeetingOccurrence = null;
        try {
            scheduledRecurrentMeetingOccurrence =
                    schedulingService.retrieveMeetingOccurrence(recurrentMeetingId, recurrentInstanceStart, recurrentInstanceId);

            logger.info("Found single occurrence of a recurrent meeting {}", scheduledRecurrentMeetingOccurrence);
        } catch (Exception e) {
            logger.warn("Failed to retrieve meeting occurrence where meeting id {}", recurrentMeetingId, e);
        }
        return scheduledRecurrentMeetingOccurrence;
    }

    public void updateMeetingOccurrence(Long recurrentInstanceStart, Meeting meeting) {
        final Long recurrentMeetingId = meeting.getMeetingId();
        logger.info("Updating single occurrence of recurrent SAL demo meeting {}", recurrentMeetingId);
        try {
            schedulingService.updateMeetingOccurrence(recurrentInstanceStart, meeting);
            logger.info("Single occurrence of SAL demo meeting {} has been updated", recurrentMeetingId);
        } catch (Exception e) {
            logger.warn("Failed to update meeting occurrence where recurrent instance start {} and meeting id {}",
                    recurrentInstanceStart, recurrentMeetingId, e);
        }
    }

    public void cancelMeetingOccurrence(Long recurrentMeetingId, Long recurrentInstanceStart, String recurrentInstanceId) {
        logger.info("Cancelling single occurrence of recurrent SAL demo meeting {}", recurrentMeetingId);
        try {
            MeetingOccurrenceCancellationRequest request =
                    new MeetingOccurrenceCancellationRequest(recurrentMeetingId, recurrentInstanceStart, recurrentInstanceId, true);
            schedulingService.cancelMeetingOccurrence(request);
            logger.info("Single occurrence of SAL demo meeting {} has been canceled", recurrentMeetingId);
        } catch (Exception e) {
            logger.warn("Failed to cancel single meeting occurrence of recurrent meeting {}", recurrentMeetingId, e);
        }
    }

    public void cancelDemoMeeting(Long meetingId) {
        logger.info("Cancelling SAL demo meeting of id {}", meetingId);

        CancellationRequest cancellationRequest = new CancellationRequest();
        cancellationRequest.setMeetingId(meetingId);
        cancellationRequest.setNotifyAttendees(true);
        try {
            schedulingService.cancelMeeting(cancellationRequest);

            logger.info("SAL demo meeting of id {} has been canceled", meetingId);
        } catch (Exception e) {
            logger.warn("Failed to cancel SAL demo meeting of id {}", meetingId, e);
        }
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getMeetingOwner() {
        return meetingOwner;
    }

    public void setMeetingOwner(String meetingOwner) {
        this.meetingOwner = meetingOwner;
    }

    public String getMeetingRequester() {
        return meetingRequester;
    }

    public void setMeetingRequester(String meetingRequester) {
        this.meetingRequester = meetingRequester;
    }

    public Long getMeetingStart() {
        return meetingStart;
    }

    public void setMeetingStart(Long meetingStart) {
        this.meetingStart = meetingStart;
    }

    public Long getMeetingEnd() {
        return meetingEnd;
    }

    public void setMeetingEnd(Long meetingEnd) {
        this.meetingEnd = meetingEnd;
    }

    public Set<EmailParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<EmailParticipant> participants) {
        this.participants = participants;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
