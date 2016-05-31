package com.bbn.nlp.corpora.ere;

import com.google.common.base.Optional;

import java.util.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;


public final class EREEntityArgument implements EREArgument {

  private final String role;
  @Nullable
  private final LinkRealis realis;
  private final EREEntityMention entityMention;
  // nullable to preserve backwards compatibility
  @Nullable
  private final EREEntity ereEntity;

  private EREEntityArgument(final String role, @Nullable final LinkRealis realis,
      final EREEntityMention entityMention, @Nullable final EREEntity ereEntity) {
    this.realis = realis;
    this.ereEntity = ereEntity;
    this.role = checkNotNull(role);
    this.entityMention = checkNotNull(entityMention);
  }

  public static EREEntityArgument from(final String role, final EREEntityMention entityMention) {
    return from(role, null, entityMention);
  }

  public static EREEntityArgument from(final String role, @Nullable final LinkRealis realis,
      final EREEntityMention entityMention) {
    return new EREEntityArgument(role, realis, entityMention, null);
  }

  public static EREEntityArgument from(final String role, @Nullable final LinkRealis realis,
      final EREEntityMention entityMention, final EREEntity ereEntity) {
    return new EREEntityArgument(role, realis, entityMention, ereEntity);
  }

  public EREEntityMention entityMention() {
    return entityMention;
  }

  public Optional<EREEntity> ereEntity() {
    return Optional.fromNullable(ereEntity);
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
  public Optional<LinkRealis> getRealis() {
    return Optional.fromNullable(realis);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EREEntityArgument that = (EREEntityArgument) o;
    return realis == that.realis &&
        Objects.equals(role, that.role) &&
        Objects.equals(entityMention, that.entityMention);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, realis, entityMention);
  }
}
