/*
 * Copyright (c) 2019 AVI-SPL Inc. All Rights Reserved.
 */

package com.avispl.symphony.sal.sample;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.avispl.symphony.api.common.error.*;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Demonstrates SAL API usage
 *
 * @author Symphony Dev Team<br> Created on Dec 8, 2018
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

    /**
     * Following properties need to be defined by an adapter implementor
     */
    private UUID accountId;
    private UUID userId;
    private String userEmail;
    private String meetingOwner;
    private String meetingRequester;
    private Long meetingStart;
    private Long meetingEnd;
    private Set<EmailParticipant> participants;

    /**
     * Reference to a {@link SchedulingService}
     * Important: in order to obtain this reference, adapter should:
     * <ul>
     *     <li>Contain a property defined as {@code private SchedulingService schedulingService;}</li>
     *     <li>Getter method for a {@code schedulingService}, see {@link #getSchedulingService()}</li>
     *     <li>Setter method for a {@code schedulingService}, see {@link #setSchedulingService(SchedulingService)}</li>
     * </ul>
     */
    private SchedulingService schedulingService;

    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    public SchedulingService getSchedulingService() {
        return this.schedulingService;
    }

    /**
     * Demonstrates using different API calls
     */
    public void scheduleAndCancelDemoMeeting() {

        // create meeting and obtain its ID
        final Long meetingId = scheduleDemoMeeting();

        // check whether meeting was successfully created
        if (meetingId == null)
            throw new RuntimeException("Meeting creation has failed");

        // retrieve created meeting
        Meeting scheduledMeeting = retrieveDemoMeeting(meetingId);

        if (scheduledMeeting == null)
            throw new RuntimeException("Meeting with meetingId=" + meetingId + " was not found");

        // update start/end time of the meeting
        scheduledMeeting = updateMeetingStartAndEnd(scheduledMeeting);

        // as we have updated meeting start value, now we have to use the
        // updated one when we want to retrieve meeting or its single occurrence
        handleDemoMeetingOccurrences(meetingId, scheduledMeeting.getStart());

        // add/remove favorite rooms
        handleRooms(accountId, userEmail);

        // add/remove custom rooms
        handleCustomRooms(userEmail);

        // retrieve meetings for given rooms
        fetchMeetingsForRooms(accountId, meetingStart, meetingEnd);

        // retrieve meetings for a given meeting requestor
        fetchMeetingsForRequester(accountId, meetingRequester, meetingStart, meetingEnd);

        // cancels created meeting
        cancelDemoMeeting(meetingId);
    }

    /**
     * Demonstrates meeting scheduling example
     */
    public Long scheduleDemoMeeting() {
        logger.info("Creating SAL demo meeting");

        // construction of meeting object
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
        meeting.setVip(false);
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

        try {
            // meeting provision via SAL API
            Long meetingId = schedulingService.createMeeting(meeting);
            logger.info("SAL demo meeting has been created with id: {}", meetingId);

            return meetingId;
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (ResourceConflictException e) {
            handleResourceConflictException(e);
        } catch (Exception e) {
            logger.info("Meeting request failed. Error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Demonstrates how to find meeting instances within given time frame and for specific meeting room identifiers
     * @param accountId account identifier to look meetings for
     * @param startDate start time of the time frame to search meeting instances
     * @param endDate end time of the time frame to search meeting instances
     */
    public void fetchMeetingsForRooms(UUID accountId, Long startDate, Long endDate) {
        // find identifiers of rooms available for an account
        List<UUID> roomIds = fetchRoomIds(accountId);

        // transform List to array
        UUID[] roomIdsArray = roomIds.toArray(new UUID[roomIds.size()]);

        try {
            // perform API call to retrieve meeting instances within given
            // time frame and for specific meeting room identifiers
            Map<UUID, Set<RoomScheduledMeeting>> roomToMeetingsMap =
                    schedulingService.listMeetingRoomSchedules(roomIdsArray, startDate, endDate);

            logger.info("Found scheduled meetings for rooms {}", roomToMeetingsMap);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to fetch meeting scheduled for rooms {}", roomIds, e);
        }
    }

    /**
     * Demonstrates various operations with recurrent meetings
     * @param meetingId meeting identifier to operate on
     * @param recurrentMeetingStart timestamp of a time instant that recurrent meeting should start on
     */
    public void handleDemoMeetingOccurrences(Long meetingId, Long recurrentMeetingStart) {
        // fetching meeting occurrence
        Meeting meetingOccurrence = retrieveMeetingOccurrence(meetingId, recurrentMeetingStart, RECURRENT_INSTANCE_ID);

        // update meeting occurrence
        updateMeetingOccurrence(recurrentMeetingStart, meetingOccurrence);

        // cancel meeting occurrence
        cancelMeetingOccurrence(meetingId, recurrentMeetingStart, RECURRENT_INSTANCE_ID);

        // retrieve meeting occurrence after a change
        retrieveMeetingOccurrence(meetingId, recurrentMeetingStart, RECURRENT_INSTANCE_ID);
    }

    /**
     * Demonstrates using room management operations via SAL API
     * @param accountId
     * @param userEmail
     */
    public void handleRooms(UUID accountId, String userEmail) {
        // fetching locations available for given account ID
        List<UUID> roomIds = fetchRoomIds(accountId);
        if (roomIds.isEmpty()) {
            throw new RuntimeException("No rooms found");
        }

        // choose random meeting room room
        UUID roomId = roomIds.get(RandomUtils.nextInt(roomIds.size()));

        // adding a favourite room
        addFavoriteRoom(userEmail, roomId);

        // fetch list of favourite rooms available for an user
        fetchFavoriteRooms(userEmail);

        // remove user's favourite room
        removeFavoriteRoom(userEmail, roomId);

        // fetch rooms recently used by an user
        fetchRecentRooms(userEmail);
    }

    /**
     * Demonstrates favourite custom rooms handling examples
     * @param userEmail user email used for a user identification for whom favourite custom rooms to be handled
     */
    public void handleCustomRooms(String userEmail) {
        // add user's favourite custom room
        addFavoriteCustomRoom(userEmail);
    }

    /**
     * Demonstrates adding user's favourite custom room
     * @param userEmail  user email used for a user identification for whom favourite custom room needs to be added
     * @return favourite custom room ID
     */
    public Long addFavoriteCustomRoom(String userEmail) {
        logger.info("Adding favorite custom room for user {}", userEmail);

        // constructing objects for an API call
        CustomRoomDetails customRoom = new CustomRoomDetails();
        customRoom.setAddress(CUSTOM_ROOM_ADDRESS);
        customRoom.setName(CUSTOM_ROOM_NAME);
        customRoom.setProtocol(CUSTOM_ROOM_PROTOCOL);
        FavoriteCustomRoomRequest request = new FavoriteCustomRoomRequest();
        request.setUserEmail(userEmail);
        request.setDetails(customRoom);

        Long customRoomId = null;
        try {
            // SAL API call to add favorite custom room
            customRoomId = schedulingService.addFavoriteCustomRoom(request);
            logger.info("Favorite custom room has been added with id {} for user {}", customRoomId, userEmail);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to add favorite custom room for user {}", userEmail, e);
        }

        return customRoomId;
    }

    /**
     * Demonstrates fetching rooms recently used by an user
     * @param userEmail user email used for a user identification for whom recent rooms need to be fetched
     */
    public void fetchRecentRooms(String userEmail) {
        logger.info("Fetching recent rooms for user {}", userEmail);
        try {
            // SAL API call to get rooms recently used by an user
            RoomListResponse response = schedulingService.listRecentRooms(userEmail);
            logger.info("Found recently used rooms {} for user {}", response, userEmail);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to fetch recent rooms for user {}", userEmail, e);
        }
    }

    /**
     * Demonstrates fetching user favourite rooms
     * @param userEmail user email used for a user identification for whom favourite rooms needs to be returned
     */
    public void fetchFavoriteRooms(String userEmail) {
        logger.info("Fetching favorite rooms for an user {}", userEmail);

        RoomListResponse response = null;
        try {
            // SAL API call to get favourite rooms
            response = schedulingService.listFavoriteRooms(userEmail);
            logger.info("Found favorite rooms {} for user {}", response, userEmail);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to fetch favorite rooms for account {}", userEmail, e);
        }
    }

    /**
     * Demonstrates removing favourite room from user's favoourite room list
     * @param userEmail user email used for a user identification for whom favourite rooms needs to be removed
     * @param roomId room identifier to remove
     */
    public void removeFavoriteRoom(String userEmail, UUID roomId) {
        logger.info("Removing favorite room {} for account {}", roomId, accountId);

        // constructing an object for an API call
        FavoriteRoomRequest request = new FavoriteRoomRequest();
        request.setUserEmail(userEmail);
        request.setRoomId(roomId);

        try {
            // SAL API call to remove favorite room
            schedulingService.removeFavoriteRoom(request);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to remove favorite room {} for account {}", roomId, accountId, e);
        }
    }

    /**
     * Demonstrates looking up meeting rooms for a given account
     * @param accountId account ID to search meeting rooms at
     * @return {@link List} identifiers of rooms available for an account
     */
    public List<UUID> fetchRoomIds(UUID accountId) {
        // fetching locations available for given account ID
        Set<Location> locations = fetchLocations(accountId);

        // picking random location ID
        UUID locationId = locations.stream()
                .map(Location::getId)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No locations available for account " + accountId));

        // fetching rooms available for the given account and location IDs
        Set<RoomDetails> rooms = fetchRooms(accountId, locationId);

        // return Set of available meeting room identifiers
        return rooms.stream()
                .map(RoomDetails::getRoomId)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates fetching rooms available for the given account and location IDs
     * @param accountId account ID to search meeting rooms at
     * @param locationId location ID to search meeting rooms at
     * @return
     */
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

    /**
     * Demonstrates looking up locations by given account ID
     * @param accountId account ID to search locations for
     * @return {@link Set} of locations available for the given account
     */
    public Set<Location> fetchLocations(UUID accountId) {
        logger.info("Fetching locations for an account ID {}", accountId);

        try {
            Set<Location> locations = schedulingService.listLocations(accountId);
            logger.info("Found locations {} for account ID {}", locations, accountId);
            return locations;
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to fetch locations by an account ID {}", accountId, e);
        }

        return Collections.emptySet();
    }

    /**
     * Demonstrates adding room to a user favorite list
     * @param userEmail user email used for a user identification for whom favourite room needs to be added
     * @param roomId room ID to add to a user favorite list
     */
    public void addFavoriteRoom(String userEmail, UUID roomId) {
        logger.info("Adding favorite room for user e-mail {} and room ID {}", userEmail, roomId);

        // constructing an object for an API call
        FavoriteRoomRequest favouriteRoomRequest = new FavoriteRoomRequest();
        favouriteRoomRequest.setUserEmail(userEmail);
        favouriteRoomRequest.setRoomId(roomId);

        try {
            // perform API call to add favourite room for an user
            schedulingService.addFavoriteRoom(favouriteRoomRequest);
            logger.info("Favorite room {} has successfully been added", favouriteRoomRequest);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to add favorite room for user e-mail {} and room ID {}", userEmail, roomId, e);
        }
    }

    /**
     * Demonstrates {@link Meeting} retrieval by a given meeting ID
     * @param meetingId meeting ID to search
     * @return {@link Meeting} instance or null if not found
     */
    public Meeting retrieveDemoMeeting(Long meetingId) {
        logger.info("Retrieving meeting of id {}", meetingId);

        Meeting scheduledMeeting = null;
        try {
            // SAL API call to fetch a meeting instance
            scheduledMeeting = schedulingService.retrieveMeeting(meetingId);
            logger.info("Found meeting {}", scheduledMeeting);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (Exception e) {
            logger.warn("Failed to retrieve meeting of id {}", meetingId, e);
        }

        return scheduledMeeting;
    }

    /**
     * Demonstrates fetching user meetings within a date range
     * @param accountId account ID to search meetings
     * @param meetingRequester meeting requestor e-mail
     * @param startDate start time of the time frame to search meeting instances
     * @param endDate end time of the time frame to search meeting instances
     */
    public void fetchMeetingsForRequester(UUID accountId, String meetingRequester, Long startDate, Long endDate) {
        logger.info("Retrieving meeting instances requested by {}", meetingRequester);
        try {
            // SAL API call to fetch user meetings within a date range
            Set<Meeting> meetings = schedulingService.listMeetingsByUsers(accountId, startDate, endDate, meetingRequester);
            logger.info("Found meeting instances {} requested by {}", meetings, meetingRequester);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to retrieve meetings requested by {}", meetingRequester, e);
        }
    }

    /**
     * Demonstrates meeting update
     * @param meeting {@link Meeting} instance to update
     * @return updated {@link Meeting} instance
     */
    public Meeting updateMeetingStartAndEnd(Meeting meeting) {
        if (meeting == null) {
            return null;
        }

        logger.info("Updating meeting {} start and end values", meeting);

        // applying changes to Meeting object
        long shift = 1000L;
        meeting.setStart(meeting.getStart() + shift);
        meeting.setEnd(meeting.getEnd() + shift);

        try {
            // SAL API call to update a meeting instance
            schedulingService.updateMeeting(meeting);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (ResourceConflictException e) {
            handleResourceConflictException(e);
        } catch (Exception e) {
            logger.warn("Failed to update scheduled SAL meeting of id {}", meeting.getMeetingId(), e);
        }

        return meeting;
    }

    /**
     * Demonstrates retrieval of single occurrence of the scheduled demo meeting.
     * This will work only if specified meeting is a recurrent one
     *
     * @param recurrentMeetingId recurrent meeting ID
     * @param recurrentInstanceStart start time of recurrent instance
     * @param recurrentInstanceId recurrent instance id (optional, needs to be provided if retrieved instance is a recurrence exception)
     */
    public Meeting retrieveMeetingOccurrence(Long recurrentMeetingId, Long recurrentInstanceStart, String recurrentInstanceId) {
        Meeting scheduledRecurrentMeetingOccurrence = null;
        try {
            // SAL API call to retrieve a meeting occurrence
            scheduledRecurrentMeetingOccurrence =
                    schedulingService.retrieveMeetingOccurrence(recurrentMeetingId, recurrentInstanceStart, recurrentInstanceId);

            logger.info("Found single occurrence of a recurrent meeting {}", scheduledRecurrentMeetingOccurrence);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (Exception e) {
            logger.warn("Failed to retrieve meeting occurrence where meeting id {}", recurrentMeetingId, e);
        }
        return scheduledRecurrentMeetingOccurrence;
    }

    /**
     * Demonstrates updating single occurrence of a recurrent meeting
     * @param recurrentInstanceStart start time of recurrent instance
     * @param meeting reccurrent meeting instance to update
     */
    public void updateMeetingOccurrence(Long recurrentInstanceStart, Meeting meeting) {
        final Long recurrentMeetingId = meeting.getMeetingId();
        logger.info("Updating single occurrence of recurrent SAL demo meeting {}", recurrentMeetingId);
        try {
            // SAL API call to update an occurrence
            schedulingService.updateMeetingOccurrence(recurrentInstanceStart, meeting);
            logger.info("Single occurrence of SAL demo meeting {} has been updated", recurrentMeetingId);
        } catch (ReferenceNotFoundException e) {
            handleReferenceNotFoundException(e);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (ResourceConflictException e) {
            handleResourceConflictException(e);
        } catch (Exception e) {
            logger.warn("Failed to update meeting occurrence where recurrent instance start {} and meeting id {}",
                    recurrentInstanceStart, recurrentMeetingId, e);
        }
    }

    /**
     * Demonstrates cancellation a single occurrence of a recurrent meeting
     * @param recurrentMeetingId identifier of a parent meeting which defines recurrent series
     * @param recurrentInstanceStart start time of recurrent instance
     * @param recurrentInstanceId recurrent instance id
     */
    public void cancelMeetingOccurrence(Long recurrentMeetingId, Long recurrentInstanceStart, String recurrentInstanceId) {
        logger.info("Cancelling single occurrence of recurrent SAL demo meeting {}", recurrentMeetingId);

        // construct object for an API call
        MeetingOccurrenceCancellationRequest request =
                new MeetingOccurrenceCancellationRequest(recurrentMeetingId, recurrentInstanceStart, recurrentInstanceId, true);

        try {
            // SAL API call to cancel an occurrence
            schedulingService.cancelMeetingOccurrence(request);
            logger.info("Single occurrence of SAL demo meeting {} has been canceled", recurrentMeetingId);
        } catch (Exception e) {
            logger.warn("Failed to cancel single meeting occurrence of recurrent meeting {}", recurrentMeetingId, e);
        }
    }

    /**
     * Demonstrates meeting cancellation by given meeting ID
     * @param meetingId meeting ID
     */
    public void cancelDemoMeeting(Long meetingId) {
        logger.info("Cancelling SAL demo meeting of id {}", meetingId);

        // construct object for an API call
        CancellationRequest cancellationRequest = new CancellationRequest();
        cancellationRequest.setMeetingId(meetingId);
        cancellationRequest.setNotifyAttendees(true);

        try {
            // SAL API call to cancel a meeting
            schedulingService.cancelMeeting(cancellationRequest);
            logger.info("SAL demo meeting of id {} has been canceled", meetingId);
        } catch (InvalidArgumentException e) {
            handleInvalidArgumentException(e);
        } catch (TargetNotFoundException e) {
            handleTargetNotFoundException(e);
        } catch (Exception e) {
            logger.warn("Failed to cancel SAL demo meeting of id {}", meetingId, e);
        }
    }

    /**
     * Demonstrates example of handling {@link ResourceConflictException} exception thrown by a SAL API.
     * See more details about {@link ResourceConflictException} in its javadoc.
     * @param e {@link ResourceConflictException} instance
     */
    private void handleResourceConflictException(ResourceConflictException e) {
        ConflictingEntity conflictingEntityType = getConflictingEntityType(e);
        logger.info("Meeting can't be provisioned because of the conflicting resource(s). "
                        + "Conflicting entity type: {}."
                        + "Conflicting entities: {}",
                conflictingEntityType,
                conflictingEntityType != null ? e.getErrorContext().get(conflictingEntityType.key()) : null);
    }

    /**
     * Demonstrates example of handling {@link ReferenceNotFoundException} exception thrown by a SAL API.
     * See more details about {@link ReferenceNotFoundException} in its javadoc.
     * @param e {@link ReferenceNotFoundException} instance
     */
    private void handleReferenceNotFoundException(ReferenceNotFoundException e) {
        InvalidEntity invalidEntityType = getInvalidEntityType(e);
        logger.info("One of the entities referenced in request wasn't found. "
                        + "Entity type not found: {}."
                        + "Object not found: {}",
                invalidEntityType,
                invalidEntityType != null ? e.getErrorContext().get(invalidEntityType.key()) : null);
    }

    /**
     * Demonstrates example of handling {@link TargetNotFoundException} exception thrown by a SAL API.
     * See more details about {@link TargetNotFoundException} in its javadoc.
     * @param e {@link ReferenceNotFoundException} instance
     */
    private void handleTargetNotFoundException(TargetNotFoundException e) {
        InvalidEntity invalidEntityType = getInvalidEntityType(e);
        logger.info("Target entity referenced in request wasn't found. "
                        + "Entity type not found: {}."
                        + "Object not found: {}",
                invalidEntityType,
                invalidEntityType != null ? e.getErrorContext().get(invalidEntityType.key()) : null);
    }

    /**
     * Demonstrates example of handling {@link InvalidArgumentException} exception thrown by a SAL API.
     * See more details about {@link InvalidArgumentException} in its javadoc.
     * @param e {@link InvalidArgumentException} instance
     */
    private void handleInvalidArgumentException(InvalidArgumentException e) {
        logger.info("Request failed due to a malformed request. "
                + "Argument reference: " +  e.getErrorContext().get(InvalidArgumentConstraints.Descriptors.ARGUMENT) + "\n"
                + "Argument value: " +  e.getErrorContext().get(InvalidArgumentConstraints.Descriptors.ARGUMENT_VALUE) + "\n"
                + "Constraint: " +  e.getErrorContext().get(InvalidArgumentConstraints.Descriptors.CONSTRAINT));
    }

    /**
     * Checks error context and returns {@link InvalidEntity} instance that's associated with the context.
     * @param e {@link ContextAwareException} instance
     * @return {@link InvalidEntity} instance that's associated with the context or null if not found
     */
    private InvalidEntity getInvalidEntityType(ContextAwareException e) {
        return getErroredEntityType(e, InvalidEntity.class);
    }

    /**
     * Checks error context and returns {@link ConflictingEntity} instance that's associated with the context.
     * @param e {@link ContextAwareException} instance
     * @return {@link ConflictingEntity} instance that's associated with the context or null if not found
     */
    private ConflictingEntity getConflictingEntityType(ContextAwareException e) {
        return getErroredEntityType(e, ConflictingEntity.class);
    }

    /**
     * Checks error context and returns enum instance that's associated with the context.
     * @param e {@link ContextAwareException} instance
     * @param c Class instance of enum to look into
     * @return instance of enum constant that's associated with the context or null if not found
     */
    private <T extends Enum & ErrorContextEntity> T  getErroredEntityType(ContextAwareException e, Class<T> c) {
        for (T ie : c.getEnumConstants()) {
            if (!e.getErrorContext().containsKey(ie.key()))
                continue;

            return ie;
        }
        return null;
    }
}
