--
-- PostgreSQL database dump
--

\restrict iwJs3PB5aTRlpPJt5Wv5OAvUU5PfFfH27hysqWcL2kSWEbCCaKL9Gl1F9qhg4sw

-- Dumped from database version 17.10
-- Dumped by pg_dump version 17.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: admin_role; Type: TYPE; Schema: public; Owner: marche_app
--

CREATE TYPE public.admin_role AS ENUM (
    'superadmin',
    'product',
    'order',
    'review',
    'inventory'
);


ALTER TYPE public.admin_role OWNER TO marche_app;

--
-- Name: cart_status; Type: TYPE; Schema: public; Owner: marche_app
--

CREATE TYPE public.cart_status AS ENUM (
    'active',
    'ordered',
    'expired'
);


ALTER TYPE public.cart_status OWNER TO marche_app;

--
-- Name: order_status; Type: TYPE; Schema: public; Owner: marche_app
--

CREATE TYPE public.order_status AS ENUM (
    'pending',
    'confirmed',
    'shipped',
    'delivered',
    'cancelled',
    'refunded'
);


ALTER TYPE public.order_status OWNER TO marche_app;

--
-- Name: review_status; Type: TYPE; Schema: public; Owner: marche_app
--

CREATE TYPE public.review_status AS ENUM (
    'pending',
    'approved',
    'rejected',
    'hidden'
);


ALTER TYPE public.review_status OWNER TO marche_app;

--
-- Name: stock_change_reason; Type: TYPE; Schema: public; Owner: marche_app
--

CREATE TYPE public.stock_change_reason AS ENUM (
    'purchase',
    'order',
    'cancel',
    'adjustment',
    'loss'
);


ALTER TYPE public.stock_change_reason OWNER TO marche_app;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admin_users; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.admin_users (
    admin_id integer NOT NULL,
    login_id character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    name character varying(50) NOT NULL,
    role public.admin_role NOT NULL,
    last_login_at timestamp without time zone
);


ALTER TABLE public.admin_users OWNER TO marche_app;

COMMENT ON TABLE public.admin_users IS '管理者アカウントを管理するテーブル';
COMMENT ON COLUMN public.admin_users.password_hash IS 'bcryptハッシュ';

CREATE SEQUENCE public.admin_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.admin_admin_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.admin_admin_id_seq OWNED BY public.admin_users.admin_id;

--
-- Name: cart; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.cart (
    cart_id integer NOT NULL,
    user_id integer NOT NULL,
    product_id integer NOT NULL,
    quantity integer DEFAULT 1 NOT NULL,
    status public.cart_status DEFAULT 'active'::public.cart_status NOT NULL
);

ALTER TABLE public.cart OWNER TO marche_app;
COMMENT ON TABLE public.cart IS 'ショッピングカートを管理するテーブル';

CREATE SEQUENCE public.cart_cart_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.cart_cart_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.cart_cart_id_seq OWNED BY public.cart.cart_id;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.categories (
    category_id integer NOT NULL,
    category_name character varying(50) NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);

ALTER TABLE public.categories OWNER TO marche_app;
COMMENT ON TABLE public.categories IS '商品カテゴリを管理するテーブル';

CREATE SEQUENCE public.category_category_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.category_category_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.category_category_id_seq OWNED BY public.categories.category_id;

--
-- Name: order_details; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.order_details (
    detail_id integer NOT NULL,
    order_id integer NOT NULL,
    product_id integer NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(10,2) NOT NULL
);

ALTER TABLE public.order_details OWNER TO marche_app;
COMMENT ON TABLE public.order_details IS '注文詳細（注文された商品の内訳）を管理するテーブル';
COMMENT ON COLUMN public.order_details.unit_price IS '注文時点の単価（スナップショット）';

CREATE SEQUENCE public.order_detail_detail_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.order_detail_detail_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.order_detail_detail_id_seq OWNED BY public.order_details.detail_id;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.orders (
    order_id integer NOT NULL,
    user_id integer NOT NULL,
    address_id integer NOT NULL,
    total_amount numeric(10,2) NOT NULL,
    status public.order_status DEFAULT 'pending'::public.order_status NOT NULL,
    payment_method character varying(30) NOT NULL,
    cancel_reason text,
    cancelled_at timestamp without time zone,
    order_at timestamp without time zone DEFAULT now() NOT NULL
);

ALTER TABLE public.orders OWNER TO marche_app;
COMMENT ON TABLE public.orders IS '注文情報を管理するテーブル';
COMMENT ON COLUMN public.orders.total_amount IS '合計金額（order_detailの集計キャッシュ値）';

CREATE SEQUENCE public.orders_order_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.orders_order_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.orders_order_id_seq OWNED BY public.orders.order_id;

--
-- Name: products; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.products (
    product_id integer NOT NULL,
    category_id integer NOT NULL,
    product_name character varying(100) NOT NULL,
    description text,
    price numeric(10,2) NOT NULL,
    stock_count integer DEFAULT 0 NOT NULL,
    image_path character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

ALTER TABLE public.products OWNER TO marche_app;
COMMENT ON TABLE public.products IS '商品情報を管理するテーブル';
COMMENT ON COLUMN public.products.stock_count IS '在庫数（stock_historyの集計キャッシュ値）';
COMMENT ON COLUMN public.products.is_deleted IS '論理削除フラグ（trueなら一覧・詳細に表示しない。注文履歴からの参照を保持するため物理削除はしない）';

CREATE SEQUENCE public.product_product_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.product_product_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.product_product_id_seq OWNED BY public.products.product_id;

--
-- Name: reviews; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.reviews (
    review_id integer NOT NULL,
    product_id integer NOT NULL,
    order_id integer NOT NULL,
    rating smallint NOT NULL,
    status public.review_status DEFAULT 'pending'::public.review_status NOT NULL,
    approved_by integer,
    approved_at timestamp without time zone,
    is_deleted boolean DEFAULT false NOT NULL,
    CONSTRAINT review_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);

ALTER TABLE public.reviews OWNER TO marche_app;
COMMENT ON TABLE public.reviews IS '商品レビューを管理するテーブル';
COMMENT ON COLUMN public.reviews.order_id IS '購入確認用の注文ID（投稿者は orders.user_id をJOINして取得）';
COMMENT ON COLUMN public.reviews.is_deleted IS '論理削除フラグ（trueなら一覧に表示しない）';

CREATE SEQUENCE public.review_review_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.review_review_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.review_review_id_seq OWNED BY public.reviews.review_id;

--
-- Name: shipping_address; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.shipping_address (
    address_id integer NOT NULL,
    user_id integer NOT NULL,
    label character varying(20),
    recipient character varying(50) NOT NULL,
    postal_code character varying(10) NOT NULL,
    prefecture character varying(20) NOT NULL,
    address_line text NOT NULL,
    is_default boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

ALTER TABLE public.shipping_address OWNER TO marche_app;
COMMENT ON TABLE public.shipping_address IS 'ユーザーの配送先住所を管理するテーブル（複数登録可）';

CREATE SEQUENCE public.shipping_address_address_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.shipping_address_address_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.shipping_address_address_id_seq OWNED BY public.shipping_address.address_id;

--
-- Name: stock_history; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.stock_history (
    history_id integer NOT NULL,
    product_id integer NOT NULL,
    admin_id integer,
    order_id integer,
    change_qty integer NOT NULL,
    reason public.stock_change_reason NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

ALTER TABLE public.stock_history OWNER TO marche_app;
COMMENT ON TABLE public.stock_history IS '在庫の増減履歴を管理するテーブル';
COMMENT ON COLUMN public.stock_history.change_qty IS '増減数（+/-）';

CREATE SEQUENCE public.stock_history_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.stock_history_history_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.stock_history_history_id_seq OWNED BY public.stock_history.history_id;

--
-- Name: users; Type: TABLE; Schema: public; Owner: marche_app
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    name character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    password_hash character varying(255) NOT NULL,
    phone character varying(20),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

ALTER TABLE public.users OWNER TO marche_app;
COMMENT ON TABLE public.users IS '会員情報を管理するテーブル';

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.users_user_id_seq OWNER TO marche_app;
ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;

--
-- Name: v_sales_by_product; Type: VIEW; Schema: public; Owner: marche_app
--

CREATE VIEW public.v_sales_by_product AS
 SELECT p.product_id,
    p.product_name,
    c.category_name,
    COALESCE(sum(od.quantity), (0)::bigint) AS total_qty,
    COALESCE(sum(((od.quantity)::numeric * od.unit_price)), (0)::numeric) AS total_sales,
    count(DISTINCT od.order_id) AS order_count,
    round(avg(od.unit_price), 2) AS avg_unit_price,
    ( SELECT count(*) AS count
           FROM public.reviews r
          WHERE ((r.product_id = p.product_id) AND (r.status = 'approved'::public.review_status))) AS review_count,
    ( SELECT round(avg(r.rating), 2) AS round
           FROM public.reviews r
          WHERE ((r.product_id = p.product_id) AND (r.status = 'approved'::public.review_status))) AS avg_rating,
    min(o.order_at) AS first_order_at,
    max(o.order_at) AS last_order_at
   FROM (((public.products p
     JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.order_details od ON ((od.product_id = p.product_id)))
     LEFT JOIN public.orders o ON (((o.order_id = od.order_id) AND (o.status <> ALL (ARRAY['cancelled'::public.order_status, 'refunded'::public.order_status])))))
  GROUP BY p.product_id, p.product_name, c.category_name;

ALTER VIEW public.v_sales_by_product OWNER TO marche_app;
COMMENT ON VIEW public.v_sales_by_product IS '商品別売上集計ビュー（キャンセル/返金注文は除外）';

--
-- Name: v_sales_by_product_monthly; Type: VIEW; Schema: public; Owner: marche_app
--

CREATE VIEW public.v_sales_by_product_monthly AS
 SELECT date_trunc('month'::text, o.order_at) AS sales_month,
    p.product_id,
    p.product_name,
    c.category_name,
    sum(od.quantity) AS total_qty,
    sum(((od.quantity)::numeric * od.unit_price)) AS total_sales,
    count(DISTINCT od.order_id) AS order_count
   FROM (((public.order_details od
     JOIN public.orders o ON (((o.order_id = od.order_id) AND (o.status <> ALL (ARRAY['cancelled'::public.order_status, 'refunded'::public.order_status])))))
     JOIN public.products p ON ((p.product_id = od.product_id)))
     JOIN public.categories c ON ((c.category_id = p.category_id)))
  GROUP BY (date_trunc('month'::text, o.order_at)), p.product_id, p.product_name, c.category_name;

ALTER VIEW public.v_sales_by_product_monthly OWNER TO marche_app;
COMMENT ON VIEW public.v_sales_by_product_monthly IS '月別×商品別売上集計ビュー（WHERE sales_month = DATE_TRUNC(''month'', 対象日) で月別絞り込み）';

--
-- Default values
--

ALTER TABLE ONLY public.admin_users ALTER COLUMN admin_id SET DEFAULT nextval('public.admin_admin_id_seq'::regclass);
ALTER TABLE ONLY public.cart ALTER COLUMN cart_id SET DEFAULT nextval('public.cart_cart_id_seq'::regclass);
ALTER TABLE ONLY public.categories ALTER COLUMN category_id SET DEFAULT nextval('public.category_category_id_seq'::regclass);
ALTER TABLE ONLY public.order_details ALTER COLUMN detail_id SET DEFAULT nextval('public.order_detail_detail_id_seq'::regclass);
ALTER TABLE ONLY public.orders ALTER COLUMN order_id SET DEFAULT nextval('public.orders_order_id_seq'::regclass);
ALTER TABLE ONLY public.products ALTER COLUMN product_id SET DEFAULT nextval('public.product_product_id_seq'::regclass);
ALTER TABLE ONLY public.reviews ALTER COLUMN review_id SET DEFAULT nextval('public.review_review_id_seq'::regclass);
ALTER TABLE ONLY public.shipping_address ALTER COLUMN address_id SET DEFAULT nextval('public.shipping_address_address_id_seq'::regclass);
ALTER TABLE ONLY public.stock_history ALTER COLUMN history_id SET DEFAULT nextval('public.stock_history_history_id_seq'::regclass);
ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);

--
-- Primary keys & unique constraints
--

ALTER TABLE ONLY public.admin_users ADD CONSTRAINT admin_login_id_key UNIQUE (login_id);
ALTER TABLE ONLY public.admin_users ADD CONSTRAINT admin_pkey PRIMARY KEY (admin_id);
ALTER TABLE ONLY public.cart ADD CONSTRAINT cart_pkey PRIMARY KEY (cart_id);
ALTER TABLE ONLY public.categories ADD CONSTRAINT category_category_name_key UNIQUE (category_name);
ALTER TABLE ONLY public.categories ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);
ALTER TABLE ONLY public.order_details ADD CONSTRAINT order_detail_pkey PRIMARY KEY (detail_id);
ALTER TABLE ONLY public.orders ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);
ALTER TABLE ONLY public.products ADD CONSTRAINT product_pkey PRIMARY KEY (product_id);
ALTER TABLE ONLY public.reviews ADD CONSTRAINT review_pkey PRIMARY KEY (review_id);
ALTER TABLE ONLY public.shipping_address ADD CONSTRAINT shipping_address_pkey PRIMARY KEY (address_id);
ALTER TABLE ONLY public.stock_history ADD CONSTRAINT stock_history_pkey PRIMARY KEY (history_id);
ALTER TABLE ONLY public.users ADD CONSTRAINT users_email_key UNIQUE (email);
ALTER TABLE ONLY public.users ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);

--
-- Indexes
--

CREATE INDEX idx_cart_product_id ON public.cart USING btree (product_id);
CREATE INDEX idx_cart_user_id ON public.cart USING btree (user_id);
CREATE INDEX idx_order_detail_order_id ON public.order_details USING btree (order_id);
CREATE INDEX idx_order_detail_product_id ON public.order_details USING btree (product_id);
CREATE INDEX idx_orders_address_id ON public.orders USING btree (address_id);
CREATE INDEX idx_orders_status ON public.orders USING btree (status);
CREATE INDEX idx_orders_user_id ON public.orders USING btree (user_id);
CREATE INDEX idx_product_category_id ON public.products USING btree (category_id);
CREATE INDEX idx_product_is_deleted ON public.products USING btree (is_deleted);
CREATE INDEX idx_product_product_name ON public.products USING btree (product_name);
CREATE INDEX idx_review_is_deleted ON public.reviews USING btree (is_deleted);
CREATE INDEX idx_review_order_id ON public.reviews USING btree (order_id);
CREATE INDEX idx_review_product_id ON public.reviews USING btree (product_id);
CREATE INDEX idx_review_status ON public.reviews USING btree (status);
CREATE INDEX idx_shipping_address_user_id ON public.shipping_address USING btree (user_id);
CREATE INDEX idx_stock_history_order_id ON public.stock_history USING btree (order_id);
CREATE INDEX idx_stock_history_product_id ON public.stock_history USING btree (product_id);

--
-- Foreign keys
--

ALTER TABLE ONLY public.cart ADD CONSTRAINT cart_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id);
ALTER TABLE ONLY public.cart ADD CONSTRAINT cart_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);
ALTER TABLE ONLY public.order_details ADD CONSTRAINT order_detail_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);
ALTER TABLE ONLY public.order_details ADD CONSTRAINT order_detail_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id);
ALTER TABLE ONLY public.orders ADD CONSTRAINT orders_address_id_fkey FOREIGN KEY (address_id) REFERENCES public.shipping_address(address_id);
ALTER TABLE ONLY public.orders ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);
ALTER TABLE ONLY public.products ADD CONSTRAINT product_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(category_id);
ALTER TABLE ONLY public.reviews ADD CONSTRAINT review_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.admin_users(admin_id);
ALTER TABLE ONLY public.reviews ADD CONSTRAINT review_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);
ALTER TABLE ONLY public.reviews ADD CONSTRAINT review_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id);
ALTER TABLE ONLY public.shipping_address ADD CONSTRAINT shipping_address_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);
ALTER TABLE ONLY public.stock_history ADD CONSTRAINT stock_history_admin_id_fkey FOREIGN KEY (admin_id) REFERENCES public.admin_users(admin_id);
ALTER TABLE ONLY public.stock_history ADD CONSTRAINT stock_history_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);
ALTER TABLE ONLY public.stock_history ADD CONSTRAINT stock_history_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id);

--
-- PostgreSQL database dump complete
--
