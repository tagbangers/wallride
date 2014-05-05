package org.wallride.config;

import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class ExtendedMySQL5InnoDBDialect extends MySQL5InnoDBDialect {

	public ExtendedMySQL5InnoDBDialect() {
		super();
		registerFunction("regexp", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "?1 REGEXP ?2"));
	}
}
