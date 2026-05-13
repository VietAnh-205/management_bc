package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.io.Serializable;

public interface FullAudited<UID extends Comparable<UID> & Serializable>
    extends Audited<UID>, DeletionAudited<UID> {}
