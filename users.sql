--
-- PostgreSQL database dump
--

-- Dumped from database version 14.18 (Homebrew)
-- Dumped by pg_dump version 14.18 (Homebrew)

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

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: kerwinlv
--

INSERT INTO "public"."users" ("id", "email", "password", "username", "avatar_url", "created_at", "role", "enabled") VALUES (3, '123456@qq.com', '$2a$10$jIwOPPJrnRrGO1x2BcIfueE5SglepeFHwMe6p0D3tfX7iz.Eoof/6', '123', NULL, NULL, NULL, true);
INSERT INTO "public"."users" ("id", "email", "password", "username", "avatar_url", "created_at", "role", "enabled") VALUES (4, 'hdihaiga@123.com', '$2a$10$4f8IQsAQtWi1Qp5rCA/3U.FTJtl7v4H/0b2vUarOoDz.Xu0diQMJO', '10235501436', '/images/default-avatar.png', NULL, NULL, true);
INSERT INTO "public"."users" ("id", "email", "password", "username", "avatar_url", "created_at", "role", "enabled") VALUES (2, 'sadhjb@kk.com', '$2a$10$vTv0XYpmRNiVsjNw7uqYoOKhfq6U/j7v7hb/BcEdoNWFy5vvtCu8G', 'kerwin2', NULL, NULL, 'ADMIN', true);
INSERT INTO "public"."users" ("id", "email", "password", "username", "avatar_url", "created_at", "role", "enabled") VALUES (1, 'kerwin19975906776@outlook.com', '$2a$10$A/bYYw3k6LHShigJY9gHtu4yTvrmC58GS76YwZKehhI3qwq2eYLbe', 'kerwin', '/user-avatars/kerwin_1752723024989.jpg', NULL, NULL, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: kerwinlv
--

SELECT pg_catalog.setval('"public"."users_id_seq"', 4, true);


--
-- PostgreSQL database dump complete
--

