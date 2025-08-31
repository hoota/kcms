DROP TABLE IF EXISTS kcms_site_property;
CREATE TABLE kcms_site_property (
	key VARCHAR NOT NULL PRIMARY KEY,
	text VARCHAR,
	date DATE,
	number BIGINT
);

insert into kcms_site_property
	select property_id as key, text, date, number from kcms_page_property where page_id=0;