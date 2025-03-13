/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.datastore.vc.service;

import ch.admin.bit.eid.datastore.shared.exceptions.ResourceNotReadyException;
import ch.admin.bit.eid.datastore.shared.model.entity.DatastoreEntity;
import ch.admin.bit.eid.datastore.shared.service.DataStoreEntityService;
import ch.admin.bit.eid.datastore.vc.model.dto.VcEntityResponseDto;
import ch.admin.bit.eid.datastore.vc.model.entity.VcEntity;
import ch.admin.bit.eid.datastore.vc.model.enums.VcTypeEnum;
import ch.admin.bit.eid.datastore.vc.model.mapper.VcEntityMapper;
import ch.admin.bit.eid.datastore.vc.repository.VcEntityRepository;
import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class VcEntityService {

    private final DataStoreEntityService dataStoreEntityService;
    private final VcEntityRepository dataStoreFileEntityRepository;

    /**
     * Extracts the resolved payload of an SD-JWT as JSON String
     *
     * @param encodedSDJWT the encoded SD-JWT with all disclosures etc.
     * @return Returns the decoded json string of the sd jwt payload
     * @throws JsonProcessingException
     */
    public String decodeSDJWTPayload(String encodedSDJWT) throws JsonProcessingException {
        // Create JSON Mapper for further JSON manipulation
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);

        // Parse into SD JWT library
        var token = SDJWT.parse(encodedSDJWT);

        // extract original payload of SD-JWT
        var rawPayload = new String(Base64.getDecoder().decode(token.getCredentialJwt().split("\\.")[1]));
        ObjectNode payloadJson = (ObjectNode) mapper.readTree(rawPayload);

        // expand original payload with disclosed values
        for (Disclosure d : token.getDisclosures()) {
            payloadJson.set(d.getClaimName(), mapper.readTree(d.getJson()).get(2));
        }

        // return JSON string
        return payloadJson.toString();
    }

    public String getVcPayload(String encodedVc, VcTypeEnum expectedType) throws Exception {
        switch (expectedType) {
            case TrustStatementV1 -> {
                return decodeSDJWTPayload(encodedVc);
            }
            default -> throw new NotImplementedException("Not supported vc type detected.");
        }
    }

    public VcEntity.VcEntityBuilder buildEmptyVcEntity(DatastoreEntity base, VcTypeEnum vcType) {
        return VcEntity.builder().vcType(vcType).base(base);
    }

    public Map<String, VcEntityResponseDto> getAllDataStoreFileEntity(UUID id) {
        DatastoreEntity base = this.dataStoreEntityService.getDataStoreEntity(id);

        List<VcEntity> presentFiles = this.dataStoreFileEntityRepository.findByBase_Id(id);

        List<VcEntity> result = new ArrayList<>(presentFiles);

        for (VcTypeEnum vcType : VcTypeEnum.values()) {
            if (presentFiles.stream().anyMatch(o -> o.getVcType() == vcType)) continue;
            result.add(this.buildEmptyVcEntity(base, vcType).build());
        }
        HashMap<String, VcEntityResponseDto> files = new HashMap<>();
        result.forEach(file -> files.put(file.getVcType().name(), VcEntityMapper.entityToVcEntityResponseDto(file)));
        return files;
    }

    @Transactional
    public void saveDataStoreFileEntity(UUID datastoreEntityId, VcEntity content) throws ResourceNotReadyException {
        DatastoreEntity base = this.dataStoreEntityService.getDataStoreEntity(datastoreEntityId);

        this.dataStoreEntityService.checkCanEdit(base);

        content.setBase(base);

        Optional<VcEntity> existing =
                this.dataStoreFileEntityRepository.findByBase_IdAndVcType(base.getId(), content.getVcType());
        existing.ifPresent(datastoreFileEntity -> content.setId(datastoreFileEntity.getId()));
        this.dataStoreFileEntityRepository.save(content);

        this.dataStoreEntityService.setActive(base);
    }
}
