-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
                              login varchar NOT NULL,
                              "password" varchar NOT NULL,
                              id uuid NOT NULL DEFAULT gen_random_uuid(),
                              CONSTRAINT users_pk PRIMARY KEY (id)
);
CREATE INDEX users_login_idx ON public.users USING btree (login);

-- Drop table

-- DROP TABLE public.tokens;

CREATE TABLE public.tokens (
                               "token" text NOT NULL,
                               date_create timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT tokens_pk PRIMARY KEY (token)
);

-- Drop table

-- DROP TABLE public.files;

CREATE TABLE public.files (
                              id_user uuid NOT NULL,
                              "name" varchar NOT NULL,
                              "data" text NULL,
                              "size" int8 NOT NULL,
                              date_insert timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              date_update timestamp NULL,
                              CONSTRAINT files_pk PRIMARY KEY (id_user, name)
);
CREATE INDEX files_id_user_idx ON public.files USING btree (id_user);

ALTER TABLE public.files ADD CONSTRAINT files_fk FOREIGN KEY (id_user) REFERENCES users(id);
