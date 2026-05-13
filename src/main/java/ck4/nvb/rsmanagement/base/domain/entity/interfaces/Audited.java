package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.io.Serializable;

public interface Audited<UID extends Comparable<UID> & Serializable>
    extends CreationAudited<UID>, ModificationAudited<UID> {}
