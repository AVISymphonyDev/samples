/*
 * Copyright (c) 2019 AVI-SPL Inc. All Rights Reserved.
 */

package com.avispl.symphony.tal.mocks;

import com.avispl.symphony.api.tal.TalAdapter;
import com.avispl.symphony.api.tal.TalProxy;
import com.avispl.symphony.api.tal.dto.Attachment;
import com.avispl.symphony.api.tal.dto.Comment;
import com.avispl.symphony.api.tal.dto.TalTicket;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Mock implementation of TalProxy
 * This mock could be used to simplify adapter testing without a need to set up Spring context and other parts of Symphony infrastructure
 *
 * @author Symphony Dev Team<br> Created on Jan 20, 2019
 */
public class MockTalProxy implements TalProxy {

    @Override
    public void pushUpdatesToTal(TalTicket talTicket) {

        if (talTicket == null)
            throw new IllegalArgumentException("talTicket must not be null");

        if (talTicket.getSymphonyId() == null || talTicket.getSymphonyId().isEmpty())
            throw new IllegalArgumentException("Field symphonyId cannot be null or empty");

        if (talTicket.getSymphonyLink() == null || talTicket.getSymphonyLink().isEmpty())
            throw new IllegalArgumentException("Field symphonyLink cannot be null or empty");

        if (talTicket.getSubject() == null || talTicket.getSubject().isEmpty())
            throw new IllegalArgumentException("Field subject cannot be null or empty");

        if (talTicket.getCustomerId() == null || talTicket.getCustomerId().isEmpty())
            throw new IllegalArgumentException("Field customer cannot be null or empty");

        if (talTicket.getDescription() == null || talTicket.getDescription().isEmpty())
            throw new IllegalArgumentException("Field description cannot be null or empty");

        if (talTicket.getPriority() == null || talTicket.getPriority().isEmpty())
            throw new IllegalArgumentException("Field priority cannot be null or empty");

        if (talTicket.getStatus() == null || talTicket.getStatus().isEmpty())
            throw new IllegalArgumentException("Field status cannot be null or empty");

        Set<Comment> comments = talTicket.getComments();
        if (comments != null) {
            for (Comment comment : comments) {

                if (comment.getCreator() == null || comment.getCreator().isEmpty())
                    throw new IllegalArgumentException("Field comment.creator cannot be null or empty");

                if (comment.getText() == null || comment.getText().isEmpty())
                    throw new IllegalArgumentException("Field comment.text cannot be null or empty");

            }
        }

        Set<Attachment> attachments = talTicket.getAttachments();
        if (attachments != null) {
            for (Attachment attachment : attachments) {

                if (attachment.getName() == null || attachment.getName().isEmpty())
                    throw new IllegalArgumentException("Field attachment.name cannot be null or empty");

                if (attachment.getCreator() == null || attachment.getCreator().isEmpty())
                    throw new IllegalArgumentException("Field attachment.creator cannot be null or empty");

                if (attachment.getLink() == null || attachment.getLink().isEmpty())
                    throw new IllegalArgumentException("Field attachment.link cannot be null or empty");

                if (attachment.getSize() == null)
                    throw new IllegalArgumentException("Field attachment.size cannot be null or empty");
            }
        }
    }

    @Override
    public void subscribeUpdates(UUID uuid, TalAdapter talAdapter) {
        // no op
    }
}
