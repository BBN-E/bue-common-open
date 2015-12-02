package com.bbn.nlp.corpora.ere;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;


public final class EREEntityArgument implements EREArgument {
  private final String role;
  private final EREEntityMention entityMention;
    
  private EREEntityArgument(final String role, final EREEntityMention entityMention) {
    this.role = checkNotNull(role);
    this.entityMention = checkNotNull(entityMention);
  }
    
  public static EREEntityArgument from(final String role, final EREEntityMention entityMention) {
    return new EREEntityArgument(role, entityMention);
  }
    
  public EREEntityMention entityMention() {
    return entityMention;
  }

  @Override
  public String getID() {
    return entityMention.getID();
  }
  
  @Override
  public String getRole() {
    return role;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(role, entityMention);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EREEntityArgument other = (EREEntityArgument) obj;
    return Objects.equal(role, other.role) && 
        Objects.equal(entityMention, other.entityMention);
  }
  
}
