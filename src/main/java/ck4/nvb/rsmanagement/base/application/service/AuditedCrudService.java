package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.Audited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import java.io.Serializable;

public interface AuditedCrudService<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & Audited<UID>,
        ID extends Comparable<ID> & Serializable,
        U extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable>
    extends CreationAuditedCrudService<D, T, ID, U, UID> {}
