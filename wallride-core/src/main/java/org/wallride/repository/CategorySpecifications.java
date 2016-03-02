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

package org.wallride.repository;

import org.springframework.data.jpa.domain.Specification;
import org.wallride.domain.Article;
import org.wallride.domain.Article_;
import org.wallride.domain.Category;
import org.wallride.domain.Category_;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class CategorySpecifications {

	public static Specification<Category> hasArticle(String language) {
		return (root, query, cb) -> {
			Subquery<Long> subquery = query.subquery(Long.class);
			Root<Article> a = subquery.from(Article.class);
			Join<Article, Category> c = a.join(Article_.categories, JoinType.INNER);
			subquery.select(c.get(Category_.id)).where(cb.equal(a.get(Article_.status), Article.Status.PUBLISHED));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(root.get(Category_.id).in(subquery));
			predicates.add(cb.equal(root.get(Category_.language), language));
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
