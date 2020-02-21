/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.entities.Collection
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.entities;

import static javax.persistence.AccessType.FIELD;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.List;
import javax.persistence.Access;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@Entity
@Access(FIELD)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Cache(usage = READ_WRITE, region = "nrg")
public class DataCollection extends AbstractHibernateEntity {
    @NotNull
    private String name;

    private String projectId;

    private String description;

    private String imageSeriesDescription;

    private String labelSeriesDescription;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<String> trainingExperiments;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<String> validationExperiments;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<String> testExperiments;
}
