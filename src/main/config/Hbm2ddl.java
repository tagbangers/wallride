import java.io.IOException;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.wallride.core.config.DataConfig;
import org.wallride.core.domain.*;

@SuppressWarnings("deprecation")
public class Hbm2ddl {
	
	public static void main(String[] args) throws IOException {
		System.getProperties().load(DataConfig.class.getResourceAsStream("/environment-develop.properties"));

		System.out.println(DomainObject.class.getPackage());

		Configuration configuration = new Configuration()
				.setProperty(Environment.DIALECT, MySQL5InnoDBDialect.class.getCanonicalName())
				.addAnnotatedClass(DomainObject.class)
				.addAnnotatedClass(Setting.class)
				.addAnnotatedClass(Post.class)
				.addAnnotatedClass(Article.class)
				.addAnnotatedClass(Page.class)
				.addAnnotatedClass(Media.class)
				.addAnnotatedClass(NavigationItem.class)
				.addAnnotatedClass(NavigationItemCategory.class)
				.addAnnotatedClass(NavigationItemPage.class)
				.addAnnotatedClass(Category.class)
				.addAnnotatedClass(Tag.class)
				.addAnnotatedClass(User.class)
				.addAnnotatedClass(UserInvitation.class);
//				.addPackage(DomainObject.class.getPackage().getName());
		new SchemaExport(configuration)
				.setDelimiter(";")
				.create(true, false);

//
//		new SchemaExport()

//		@SuppressWarnings("resource")
//		ApplicationContext context = new AnnotationConfigApplicationContext(DataConfig.class);
//		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = context.getBean(LocalContainerEntityManagerFactoryBean.class);
//
//		Configuration cfg = new Ejb3Configuration().configure(entityManagerFactoryBean.getPersistenceUnitInfo(), null).getHibernateConfiguration();
//		new SchemaExport(cfg).setDelimiter(";").create(true, false);
	}
}
