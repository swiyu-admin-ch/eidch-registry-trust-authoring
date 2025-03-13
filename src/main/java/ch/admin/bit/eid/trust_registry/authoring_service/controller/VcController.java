/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.trust_registry.authoring_service.controller;

import ch.admin.bit.eid.datastore.shared.exceptions.ApiError;
import ch.admin.bit.eid.datastore.shared.model.dto.DatastoreEntityResponseDto;
import ch.admin.bit.eid.datastore.shared.model.entity.DatastoreEntity;
import ch.admin.bit.eid.datastore.shared.model.mapper.DatastoreEntityMapper;
import ch.admin.bit.eid.datastore.shared.service.DataStoreEntityService;
import ch.admin.bit.eid.datastore.vc.model.enums.VcTypeEnum;
import ch.admin.bit.eid.datastore.vc.service.VcEntityService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/truststatement")
@AllArgsConstructor
@Tag(name = "Trust Statement", description = "API for Managing VC entries in the datastore.")
public class VcController {

    private final DataStoreEntityService datastoreEntityService;
    private final VcEntityService datastoreFileEntityService;

    @Timed
    @PutMapping(value = "/{truststoreEntryID}")
    @Operation(summary = "Update a trust statement v1 entry in the datastore.",
            description = "Update a trust statement v1 entry in the datastore.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success", useReturnTypeSchema = true),
                    @ApiResponse(
                            responseCode = "425",
                            description = "Too Early, Resource cannot be edited.",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),
            }
    )
    public DatastoreEntityResponseDto updateTrustStatementV1(
            @Valid @PathVariable UUID truststoreEntryID,
            @RequestBody @Schema(description = "The encoded VC to put into that trust store entry.", example = "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDpleGFtcGxlOmdvdiNrZXkwMSJ9.eyJpc3MiOiJkaWQ6ZXhhbXBsZTp0cnVzdC5hZG1pbi5jaDpHb3YiLCJfc2QiOlsiOU5oZEt5ajV4c3FEYm9ySTZlbnVqSndUUW8xSWpXQVhjSWtvYkNYU1RfcyIsIlZCNkRrYVJxVDhrWU1kOVl2dGZtQnZLaUFUeGU5Vk1kUHY2Unc1d2N5U3ciLCJZa0RZMnVueE5SUzJuRTlRY2xBYmpFX3Fzb1FrOWdMSEstZk9teFJkZi1jIiwiYVBnYUFyR2RTeF9hMTNOSE9nbUwxVTc5T0tyQXhpSmRXemgzR3F0U1V5RSIsImpjS0hYLV9JVkp0VG1yMXNJYkNiTTI3MlRRWjM4dGFDTENxcEdIRFVCX3MiXSwibmJmIjoxNzIyNDk5MjAwLCJleHAiOjE3NjcxNjgwMDAsImlhdCI6MTcyNjA1NjQwOCwiX3NkX2FsZyI6InNoYS0yNTYifQ._E4xEzimpbJhCEoV6tu_PKUuSWukMcEF7gxpJc0yBzUE0piYWTCrYGI72mQSaaK5yYHgbknVrokY5JbeON1EdQ~WyI4cjE4VlpMbXZJbHpzelNobWwyYklnIiwic3ViIiwiZGlkOmV4YW1wbGU6dHJ1c3QuYWRtaW4uY2g6cHJpdmF0ZUFjdG9yQSJd~WyJDNXdWVFloOWJFM2x0R2RENWNMMWtBIiwib3JnTmFtZSIseyJlbiI6IlN3aXNzIENvbmZlZGVyYXRpb24iLCJkZS1DSCI6IlNjaHdlaXplciBFaWRnZW5vc3NlbnNjaGFmdCJ9XQ~WyJsTkZqY09lUUZHSE1HRGstQV9LUXFBIiwicHJlZkxhbmciLCJlbiJd~WyJod2hiWWc3XzVKN09hMzVLN08tZGpRIiwidmN0IiwiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIl0~WyI1WGgxMHQ1ZU1zOEdOOGN4WkRCR0RRIiwibG9nb1VyaSIseyJlbiI6ImRhdGE6aW1hZ2UvcG5nO2Jhc2U2NCxpVkJPUncwS0dnb0FBQUFOU1VoRVVnQUFBRndBQUFCY0NBTUFBQURVTVNKcUFBQUFHRkJNVkVYL0FBRC8vLy8vdzhQLzM5Ly9yS3ovNCtQL3Y3Ly82K3RoL3lreUFBQUFiMGxFUVZSb2dlM1lRUXJBSUF3RlVXdE12UCtOdTI1MEVjUVVhbWZXOHBiNVlDbEVSRVJiYWlxUHRHM0U2K1dxNE9EZzRPRGdiK0xtY1Z1RVdoMnk3dkZ1NDZ2SXJxcUhvbWtBbDFWY3dNSEJ3VS9FVTA5dTZsak0yalp6czc2Ny91RGc0T0RnWitDcG41WkVSUFRyYnRyNUNGMzYvZFZCQUFBQUFFbEZUa1N1UW1DQyIsImRlLUNIIjoiZGF0YTppbWFnZS9wbmc7YmFzZTY0LGlWQk9SdzBLR2dvQUFBQU5TVWhFVWdBQUFGd0FBQUJjQ0FNQUFBRFVNU0pxQUFBQUdGQk1WRVgvQUFELy8vLy93OFAvMzkvL3JLei80K1AvdjcvLzYrdGgveWt5QUFBQWIwbEVRVlJvZ2UzWVFRckFJQXdGVVd0TXZQK051MjUwRWNRVWFtZlc4cGI1WUNsRVJFUmJhaXFQdEczRTYrV3E0T0RnNE9EZ2IrTG1jVnVFV2gyeTd2RnU0NnZJcnFxSG9ta0FsMVZjd01IQndVL0VVMDl1NmxqTTJqWnpzNzY3L3VEZzRPRGdaK0NwbjVaRVJQVHJidHI1Q0YzNi9kVkJBQUFBQUVsRlRrU3VRbUNDIn1d~") String body
    ) throws Exception {
        DatastoreEntity datastoreEntity = this.datastoreEntityService.getDataStoreEntity(truststoreEntryID);

        var vcPayload = this.datastoreFileEntityService.getVcPayload(body, VcTypeEnum.TrustStatementV1);

        this.datastoreFileEntityService.saveDataStoreFileEntity(
                truststoreEntryID,
                this.datastoreFileEntityService.buildEmptyVcEntity(datastoreEntity, VcTypeEnum.TrustStatementV1)
                        .rawVc(body)
                        .vcPayload(vcPayload)
                        .build()
        );

        return DatastoreEntityMapper.entityToDatastoreEntityResponseDto(
                datastoreEntity,
                this.datastoreFileEntityService.getAllDataStoreFileEntity(truststoreEntryID)
        );
    }
}
