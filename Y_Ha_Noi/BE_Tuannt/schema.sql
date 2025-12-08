--
-- PostgreSQL database dump
--

\restrict 3mt1Xc2a7DbfqySytdYCmzmXg9ejnAjmxNvRCyVKp95p3Sdq88dKq2hofQzfUEx

-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: departments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.departments (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    handler_id bigint,
    name character varying(100) NOT NULL,
    notification_email character varying(100),
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    manager_id bigint,
    CONSTRAINT departments_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.departments OWNER TO postgres;

--
-- Name: departments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.departments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.departments_id_seq OWNER TO postgres;

--
-- Name: departments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.departments_id_seq OWNED BY public.departments.id;


--
-- Name: doctors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctors (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    department_id bigint NOT NULL,
    email character varying(100),
    full_name character varying(100) NOT NULL,
    phone character varying(20),
    specialty character varying(100),
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT doctors_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.doctors OWNER TO postgres;

--
-- Name: doctors_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.doctors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.doctors_id_seq OWNER TO postgres;

--
-- Name: doctors_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.doctors_id_seq OWNED BY public.doctors.id;


--
-- Name: feedback_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feedback_history (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    created_by bigint NOT NULL,
    feedback_id bigint NOT NULL,
    note text,
    status character varying(20) NOT NULL,
    image_ids text,
    content text,
    CONSTRAINT feedback_history_status_check CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'ASSIGNED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.feedback_history OWNER TO postgres;

--
-- Name: COLUMN feedback_history.image_ids; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feedback_history.image_ids IS 'JSON array of image IDs, e.g., "[1,2,3]"';


--
-- Name: COLUMN feedback_history.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feedback_history.content IS 'Nội dung xử lý (required)';


--
-- Name: feedback_history_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.feedback_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.feedback_history_id_seq OWNER TO postgres;

--
-- Name: feedback_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.feedback_history_id_seq OWNED BY public.feedback_history.id;


--
-- Name: feedback_images; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feedback_images (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    feedback_id bigint NOT NULL,
    file_path character varying(500) NOT NULL,
    filename character varying(255) NOT NULL,
    image_type character varying(20) NOT NULL,
    CONSTRAINT feedback_images_image_type_check CHECK (((image_type)::text = ANY ((ARRAY['FEEDBACK'::character varying, 'PROCESS'::character varying])::text[])))
);


ALTER TABLE public.feedback_images OWNER TO postgres;

--
-- Name: feedback_images_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.feedback_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.feedback_images_id_seq OWNER TO postgres;

--
-- Name: feedback_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.feedback_images_id_seq OWNED BY public.feedback_images.id;


--
-- Name: feedbacks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feedbacks (
    id bigint NOT NULL,
    channel character varying(20) NOT NULL,
    code character varying(50) NOT NULL,
    completed_date date,
    content text NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    department_id bigint,
    doctor_id bigint,
    handler_id bigint,
    level character varying(20) NOT NULL,
    received_date date NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    receiver_id bigint,
    CONSTRAINT feedbacks_channel_check CHECK (((channel)::text = ANY ((ARRAY['PHONE'::character varying, 'EMAIL'::character varying, 'WEBSITE'::character varying, 'DIRECT'::character varying])::text[]))),
    CONSTRAINT feedbacks_level_check CHECK (((level)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying])::text[]))),
    CONSTRAINT feedbacks_status_check CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'ASSIGNED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.feedbacks OWNER TO postgres;

--
-- Name: feedbacks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.feedbacks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.feedbacks_id_seq OWNER TO postgres;

--
-- Name: feedbacks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.feedbacks_id_seq OWNED BY public.feedbacks.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    message text NOT NULL,
    read boolean NOT NULL,
    title character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    user_id bigint NOT NULL,
    feedback_id bigint,
    rating_id bigint,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['FEEDBACK'::character varying, 'ASSIGNED'::character varying, 'COMPLETED'::character varying, 'RATING'::character varying])::text[])))
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notifications_id_seq OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: ratings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ratings (
    id bigint NOT NULL,
    comment text,
    created_at timestamp(6) without time zone NOT NULL,
    doctor_id bigint,
    feedback_id bigint NOT NULL,
    rating integer NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id bigint NOT NULL
);


ALTER TABLE public.ratings OWNER TO postgres;

--
-- Name: ratings_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ratings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ratings_id_seq OWNER TO postgres;

--
-- Name: ratings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ratings_id_seq OWNED BY public.ratings.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    department_id bigint,
    email character varying(100),
    full_name character varying(100),
    password character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    username character varying(50) NOT NULL,
    phone character varying(20),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'LEADER'::character varying, 'RECEIVER'::character varying, 'HANDLER'::character varying, 'VIEWER'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: departments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments ALTER COLUMN id SET DEFAULT nextval('public.departments_id_seq'::regclass);


--
-- Name: doctors id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctors ALTER COLUMN id SET DEFAULT nextval('public.doctors_id_seq'::regclass);


--
-- Name: feedback_history id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback_history ALTER COLUMN id SET DEFAULT nextval('public.feedback_history_id_seq'::regclass);


--
-- Name: feedback_images id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback_images ALTER COLUMN id SET DEFAULT nextval('public.feedback_images_id_seq'::regclass);


--
-- Name: feedbacks id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks ALTER COLUMN id SET DEFAULT nextval('public.feedbacks_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: ratings id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ratings ALTER COLUMN id SET DEFAULT nextval('public.ratings_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (id);


--
-- Name: doctors doctors_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctors
    ADD CONSTRAINT doctors_pkey PRIMARY KEY (id);


--
-- Name: feedback_history feedback_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback_history
    ADD CONSTRAINT feedback_history_pkey PRIMARY KEY (id);


--
-- Name: feedback_images feedback_images_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback_images
    ADD CONSTRAINT feedback_images_pkey PRIMARY KEY (id);


--
-- Name: feedbacks feedbacks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks
    ADD CONSTRAINT feedbacks_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: ratings ratings_feedback_id_user_id_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_feedback_id_user_id_unique UNIQUE (feedback_id, user_id);


--
-- Name: ratings ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: feedbacks uk_fdxix8hsx8l4looqgqjqo2hsp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks
    ADD CONSTRAINT uk_fdxix8hsx8l4looqgqjqo2hsp UNIQUE (code);


--
-- Name: departments uk_l7tivi5261wxdnvo6cct9gg6t; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT uk_l7tivi5261wxdnvo6cct9gg6t UNIQUE (code);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: doctors uk_sqdclqa7yj7fidm167b8h5tvi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctors
    ADD CONSTRAINT uk_sqdclqa7yj7fidm167b8h5tvi UNIQUE (code);


--
-- Name: ratings ukow87ff7xkybl37idtgryb2l54; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ukow87ff7xkybl37idtgryb2l54 UNIQUE (feedback_id, user_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: ratings ratings_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict 3mt1Xc2a7DbfqySytdYCmzmXg9ejnAjmxNvRCyVKp95p3Sdq88dKq2hofQzfUEx

