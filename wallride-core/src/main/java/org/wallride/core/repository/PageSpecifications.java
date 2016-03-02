/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.core.repository;

import org.springframework.data.jpa.domain.Specification;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Page_;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public class PageSpecifications {

	public static Specification<Page> draft(Page page) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get(Page_.drafted), page));

			Subquery<Long> subquery = query.subquery(Long.class);
			Root<Page> p = subquery.from(Page.class);
			subquery.select(cb.max(p.get(Page_.id))).where(cb.equal(p.get(Page_.drafted), page));

			predicates.add(cb.equal(root.get(Page_.id), subquery));
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public static Specification<Page> path(Page page) {
		return path(page, false);
	}

	public static Specification<Page> path(Page page, boolean includeUnpublished) {
		return (root, query, cb) -> {
//			query.distinct(true);
			Root<Page> p1 = query.from(Page.class);
			Root<Page> p2 = root;
			Root<Page> p3 = query.from(Page.class);

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get(Page_.language), page.getLanguage()));
			if (!includeUnpublished) {
				predicates.add(cb.equal(root.get(Page_.status), Page.Status.PUBLISHED));
			}

			Subquery<Long> subquery = query.subquery(Long.class);
			Root<Page> p = subquery.from(Page.class);
			subquery.select(p.get(Page_.id)).where(
					cb.equal(p.get(Page_.language), page.getLanguage()),
					cb.isNull(p.get(Page_.parent)),
					cb.le(p.get(Page_.lft), page.getLft()),
					cb.ge(p.get(Page_.rgt), page.getRgt()));

			predicates.add(cb.equal(p1.get(Page_.id), subquery));
			predicates.add(cb.equal(p3.get(Page_.id), page.getId()));
			predicates.add(cb.between(p2.get(Page_.lft), p1.get(Page_.lft), p1.get(Page_.rgt)));
			predicates.add(cb.between(p3.get(Page_.lft), p2.get(Page_.lft), p2.get(Page_.rgt)));
			query.orderBy(cb.desc(cb.diff(p2.get(Page_.rgt), p2.get(Page_.lft))));
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public static Specification<Page> children(Page page) {
		return children(page, false);
	}

	public static Specification<Page> children(Page page, boolean includeUnpublished) {
		return (root, query, cb) -> {
			query.distinct(true);
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get(Page_.parent), page));
			if (!includeUnpublished) {
				predicates.add(cb.equal(root.get(Page_.status), Page.Status.PUBLISHED));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public static Specification<Page> siblings(Page page) {
		return siblings(page, false);
	}

	public static Specification<Page> siblings(Page page, boolean includeUnpublished) {
		return (root, query, cb) -> {
			query.distinct(true);
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get(Page_.parent), page.getParent()));
			if (!includeUnpublished) {
				predicates.add(cb.equal(root.get(Page_.status), Page.Status.PUBLISHED));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
