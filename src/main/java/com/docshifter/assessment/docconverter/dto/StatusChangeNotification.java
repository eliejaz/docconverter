package com.docshifter.assessment.docconverter.dto;

import com.docshifter.assessment.docconverter.model.DocumentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class StatusChangeNotification {
    private Long fileId;
    private String convertedName;
    private String convertedFilePath;
    private LocalDateTime convertedAt;
    private DocumentStatus newDocumentStatus;
}
