package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;

import java.util.Collection;

/**
 * An {@link Alignment} which also tracks "provenances" (which can be arbitrary metadata) for the
 * items being aligned.
 *
 * @param <LeftT>      This class is covariant on this parameter.
 * @param <LeftProvT>  This class is covariant on this parameter.
 * @param <RightProvT> This class is covariant on this parameter.
 */
@Beta
public interface ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>
    extends Alignment<LeftT, RightT> {

  Collection<LeftProvT> provenancesForLeftItem(LeftT item);

  Collection<RightProvT> provenancesForRightItem(RightT item);
}
