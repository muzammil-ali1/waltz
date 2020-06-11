/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.email;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.common.StringUtilities;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class WaltzEmailer {

    private static final Logger LOG = LoggerFactory.getLogger(WaltzEmailer.class);
    private static final String DEFAULT_EMAIL_TEMPLATE_LOCATION = "/templates/waltz-email-template.ftlh";

    private final JavaMailSender mailSender;

    @Value("${waltz.from.email}")
    private String fromEmail;


    @Autowired
    public WaltzEmailer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendEmail(String subject,
                          String body,
                          String[] to) {

        if (this.mailSender == null) {
            LOG.warn("Not sending email.  No mailer provided.");
            return;
        }
        Checks.checkNotEmpty(subject, "subject cannot be empty");
        Checks.checkNotEmpty(body, "body cannot be empty");
        Checks.checkNotEmpty(to, "to cannot be empty");
        Checks.checkAll(to, StringUtilities::notEmpty, "email address cannot be empty");

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
            message.setSubject(subject);
            message.setFrom(fromEmail);
            message.setBcc(to);
            message.addAttachment("waltz.png", IOUtilities.getFileResource("/images/waltz.png"));
            message.addAttachment("client-logo", IOUtilities.getFileResource("/templates/images/client-logo.png"));

            Map model = new HashMap();
            model.put("body", body);

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

            try(InputStreamReader templateReader = new InputStreamReader(IOUtilities
                    .getFileResource(DEFAULT_EMAIL_TEMPLATE_LOCATION)
                    .getInputStream())) {
                Template template = new Template("template", templateReader, cfg);
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                message.setText(text, true);
            }
        };

        this.mailSender.send(preparator);
    }
}
