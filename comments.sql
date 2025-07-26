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
-- Data for Name: article_comments; Type: TABLE DATA; Schema: public; Owner: kerwinlv
--

INSERT INTO "public"."article_comments" ("id", "content", "created_at", "updated_at", "news_article_id", "parent_comment_id", "user_id") VALUES (1, 'good', '2025-07-17 11:30:12.504147', NULL, 1573, NULL, 1);


--
-- Name: article_comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: kerwinlv
--

SELECT pg_catalog.setval('"public"."article_comments_id_seq"', 1, true);


--
-- PostgreSQL database dump complete
--

