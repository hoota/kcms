DROP TABLE IF EXISTS page CASCADE;
CREATE TABLE page (
	id BIGSERIAL PRIMARY KEY,
	slug VARCHAR NOT NULL,
	title VARCHAR NOT NULL,
	template VARCHAR NOT NULL
);

DROP TABLE IF EXISTS page_property;
CREATE TABLE page_property (
	page_id BIGINT NOT NULL,
	widget_id VARCHAR NOT NULL,
	property_id VARCHAR NOT NULL,
	text VARCHAR,
	date DATE,
	number BIGINT,

	PRIMARY KEY(page_id, widget_id, property_id),
    FOREIGN KEY (page_id) REFERENCES public.page(id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO page VALUES(0, '', '', '');