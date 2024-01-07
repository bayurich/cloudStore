delete from public.users;
INSERT INTO public.users(login, "password")
VALUES('test_user', 'test_password');
commit;
