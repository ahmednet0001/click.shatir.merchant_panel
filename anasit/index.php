<?php
// anasit.clude - Corporate Website
// الشركة: أنس لتكنولوجيا المعلومات
?>
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>أنس لتكنولوجيا المعلومات | anasit</title>
    <meta name="description" content="أنس لتكنولوجيا المعلومات - حلول تقنية متكاملة تشمل Shatir.click وخدمات SaaS والبريد الإلكتروني الاحترافي للشركات.">
    <meta name="keywords" content="أنس لتكنولوجيا المعلومات, anasit, Shatir, SaaS, بريد إلكتروني للشركات, حلول تقنية">
    <meta name="author" content="anasit">

    <meta property="og:title" content="أنس لتكنولوجيا المعلومات | anasit">
    <meta property="og:description" content="حلول تقنية متكاملة لنمو أعمالك.">
    <meta property="og:type" content="website">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <style>
        :root {
            --navy: #082d63;
            --blue: #075cff;
            --cyan: #16b8ef;
            --text: #17345f;
            --muted: #687895;
            --light: #f5f9ff;
            --border: #e4ecf7;
            --white: #fff;
            --radius: 22px;
            --shadow: 0 14px 40px rgba(15, 53, 105, .08);
        }

        * { box-sizing: border-box; margin: 0; padding: 0; }

        html { scroll-behavior: smooth; }

        body {
            font-family: "Cairo", Tahoma, Arial, sans-serif;
            color: var(--text);
            background: #fff;
            line-height: 1.8;
        }

        a { color: inherit; text-decoration: none; }

        .container {
            width: min(1180px, calc(100% - 40px));
            margin-inline: auto;
        }

        /* Header */
        header {
            height: 88px;
            display: flex;
            align-items: center;
            background: rgba(255,255,255,.94);
            border-bottom: 1px solid #edf2f8;
            position: sticky;
            top: 0;
            z-index: 100;
            backdrop-filter: blur(12px);
        }

        .nav {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 30px;
        }

        .brand {
            display: inline-flex;
            align-items: center;
            gap: 12px;
        }

        .brand-mark {
            width: 47px;
            height: 47px;
        }

        .brand-text {
            display: flex;
            flex-direction: column;
            line-height: 1.05;
        }

        .brand-text strong {
            font-family: Arial, sans-serif;
            font-size: 30px;
            letter-spacing: -1.5px;
            color: #092f67;
        }

        .brand-text span {
            font-size: 11px;
            color: #174b8e;
            margin-top: 4px;
        }

        .nav-links {
            display: flex;
            align-items: center;
            gap: 42px;
            font-size: 15px;
            font-weight: 600;
        }

        .nav-links a {
            color: #33486a;
            transition: .2s;
        }

        .nav-links a:hover,
        .nav-links a.active { color: var(--blue); }

        .nav-button {
            background: var(--blue);
            color: #fff !important;
            padding: 9px 21px;
            border-radius: 12px;
        }

        /* Hero */
        .hero {
            padding: 28px 0 0;
        }

        .hero-box {
            min-height: 475px;
            border-radius: 13px;
            overflow: hidden;
            position: relative;
            display: flex;
            align-items: center;
            background:
                radial-gradient(circle at 75% 40%, rgba(36,177,245,.20), transparent 27%),
                linear-gradient(110deg, #f7fbff 0%, #eef7ff 53%, #dcecff 100%);
        }

        .hero-content {
            width: 52%;
            padding: 58px 7% 58px 0;
            position: relative;
            z-index: 2;
        }

        .hero-content h1 {
            font-size: clamp(38px, 4vw, 60px);
            line-height: 1.25;
            color: var(--navy);
            font-weight: 800;
            margin-bottom: 18px;
        }

        .hero-content h1 span { color: var(--blue); }

        .hero-content p {
            color: #657590;
            font-size: 18px;
            max-width: 560px;
            margin-bottom: 28px;
        }

        .primary-btn {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            background: linear-gradient(135deg, #0861ff, #0752df);
            color: #fff;
            padding: 13px 29px;
            border-radius: 13px;
            font-weight: 700;
            box-shadow: 0 10px 24px rgba(7,92,255,.20);
            transition: transform .2s, box-shadow .2s;
        }

        .primary-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 14px 30px rgba(7,92,255,.27);
        }

        .hero-visual {
            position: absolute;
            inset: 0 0 0 0;
            pointer-events: none;
        }

        .hero-glow {
            position: absolute;
            width: 430px;
            height: 430px;
            border-radius: 50%;
            background: rgba(26,168,239,.11);
            right: 16%;
            top: 25px;
            filter: blur(3px);
        }

        .hero-logo {
            position: absolute;
            right: 18%;
            top: 76px;
            width: 290px;
            text-align: center;
        }

        .hero-logo .mark {
            width: 125px;
            height: 125px;
            margin: auto;
        }

        .hero-logo h2 {
            font-family: Arial, sans-serif;
            font-size: 58px;
            line-height: 1;
            letter-spacing: -3px;
            color: #092f67;
            margin-top: 10px;
        }

        .hero-logo p {
            color: #174b8e;
            font-size: 18px;
            margin-top: 5px;
        }

        .laptop {
            position: absolute;
            left: 4%;
            bottom: 0;
            width: 390px;
            height: 245px;
            background: linear-gradient(135deg, #9aa6b8, #26384e);
            border-radius: 18px 18px 5px 5px;
            transform: perspective(700px) rotateY(-8deg);
            box-shadow: -15px 20px 45px rgba(16,40,73,.20);
            opacity: .92;
        }

        .laptop:before {
            content: "";
            position: absolute;
            inset: 11px;
            background: linear-gradient(135deg, #e9f5ff, #b8dcf4);
            border-radius: 10px;
        }

        .laptop:after {
            content: "";
            position: absolute;
            width: 450px;
            height: 14px;
            background: #65758a;
            left: -30px;
            bottom: -12px;
            border-radius: 0 0 15px 15px;
        }

        /* Sections */
        .section {
            padding: 68px 0;
        }

        .section-heading {
            text-align: center;
            margin-bottom: 38px;
        }

        .section-heading h2 {
            color: var(--navy);
            font-size: 38px;
            font-weight: 800;
            display: inline-flex;
            align-items: center;
            gap: 28px;
        }

        .section-heading h2:before,
        .section-heading h2:after {
            content: "";
            width: 48px;
            height: 2px;
            background: var(--blue);
        }

        .section-heading p {
            color: var(--muted);
            margin-top: 8px;
            font-size: 16px;
        }

        .services {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 25px;
        }

        .service {
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 32px 27px 29px;
            text-align: center;
            background: #fff;
            box-shadow: var(--shadow);
            transition: .25s;
        }

        .service:hover {
            transform: translateY(-6px);
            box-shadow: 0 20px 50px rgba(15,53,105,.12);
        }

        .service-icon {
            width: 78px;
            height: 78px;
            margin: 0 auto 19px;
            border-radius: 22px;
            display: grid;
            place-items: center;
            color: var(--blue);
            background: #edf5ff;
        }

        .service-icon svg {
            width: 38px;
            height: 38px;
        }

        .service h3 {
            font-size: 21px;
            margin-bottom: 9px;
            color: var(--navy);
        }

        .service p {
            color: var(--muted);
            min-height: 76px;
            font-size: 14px;
        }

        .service-link {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: var(--blue);
            font-weight: 700;
            margin-top: 15px;
        }

        /* About */
        .about {
            background: var(--light);
        }

        .about-grid {
            display: grid;
            grid-template-columns: 1.1fr .9fr;
            gap: 70px;
            align-items: center;
        }

        .about h2 {
            font-size: 38px;
            color: var(--navy);
            margin-bottom: 15px;
        }

        .about p {
            color: var(--muted);
            font-size: 17px;
        }

        .about-card {
            background: #fff;
            padding: 30px;
            border-radius: 24px;
            border: 1px solid var(--border);
            box-shadow: var(--shadow);
        }

        .about-card strong {
            display: block;
            color: var(--navy);
            font-size: 20px;
            margin-bottom: 8px;
        }

        .about-card + .about-card { margin-top: 15px; }

        /* Contact */
        .contact {
            text-align: center;
        }

        .contact-box {
            background: linear-gradient(135deg, #082d63, #0b438d);
            color: #fff;
            border-radius: 27px;
            padding: 48px 25px;
        }

        .contact-box h2 {
            font-size: 34px;
            margin-bottom: 8px;
        }

        .contact-box p {
            color: #d7e7ff;
            margin-bottom: 23px;
        }

        .contact-box .primary-btn {
            background: #fff;
            color: var(--blue);
            box-shadow: none;
        }

        /* Footer */
        footer {
            margin-top: 70px;
            background: #0a2854;
            color: #fff;
            text-align: center;
            padding: 32px 20px;
        }

        footer .footer-logo {
            font-family: Arial, sans-serif;
            font-size: 30px;
            font-weight: 700;
            letter-spacing: -1px;
        }

        footer p {
            color: #b7c9e5;
            font-size: 13px;
            margin-top: 4px;
        }

        .copyright {
            margin-top: 17px;
            padding-top: 17px;
            border-top: 1px solid rgba(255,255,255,.12);
            color: #93abd0;
            font-size: 12px;
        }

        /* Mobile */
        @media (max-width: 900px) {
            .nav-links { gap: 18px; }
            .hero-content { width: 65%; }
            .hero-logo { right: 4%; opacity: .75; }
            .laptop { opacity: .22; }
            .services { grid-template-columns: 1fr; max-width: 550px; margin: auto; }
            .about-grid { grid-template-columns: 1fr; gap: 30px; }
        }

        @media (max-width: 650px) {
            .container { width: min(100% - 28px, 1180px); }
            header { height: 74px; }
            .brand-mark { width: 40px; height: 40px; }
            .brand-text strong { font-size: 25px; }
            .brand-text span { font-size: 9px; }
            .nav-links a:not(.nav-button) { display: none; }
            .nav-button { padding: 7px 14px; font-size: 13px; }

            .hero-box { min-height: 520px; }
            .hero-content {
                width: 100%;
                padding: 45px 25px;
                text-align: center;
            }
            .hero-content p { font-size: 15px; }
            .hero-logo {
                right: 50%;
                transform: translateX(50%);
                top: 235px;
                opacity: .12;
            }
            .hero-logo h2 { font-size: 48px; }
            .laptop { display: none; }

            .section { padding: 52px 0; }
            .section-heading h2 { font-size: 30px; gap: 14px; }
            .section-heading h2:before,
            .section-heading h2:after { width: 25px; }

            .about h2 { font-size: 30px; }
            .contact-box h2 { font-size: 27px; }
        }
    </style>
</head>

<body>

<header>
    <div class="container nav">
        <a href="#home" class="brand" aria-label="anasit">
            <svg class="brand-mark" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 76C17 72 18 67 20 62L48 15C52 8 60 7 64 14L82 46C85 52 82 59 76 61L61 66L48 43L37 62L31 75C29 80 23 81 20 76Z" fill="#075CFF"/>
                <path d="M47 43L61 66L82 46L64 14C61 9 55 10 51 16L39 37L47 43Z" fill="#0A3B91"/>
                <path d="M37 62L47 43L61 66L44 78C38 82 32 75 37 62Z" fill="#16B8EF"/>
            </svg>

            <span class="brand-text">
                <strong>anasit</strong>
                <span>أنس لتكنولوجيا المعلومات</span>
            </span>
        </a>

        <nav class="nav-links">
            <a href="#home" class="active">الرئيسية</a>
            <a href="#services">الخدمات</a>
            <a href="#about">من نحن</a>
            <a href="#contact" class="nav-button">تواصل معنا</a>
        </nav>
    </div>
</header>

<main>

    <section class="hero" id="home">
        <div class="container">
            <div class="hero-box">

                <div class="hero-content">
                    <h1>حلول تقنية متكاملة<br><span>لنمو أعمالك</span></h1>
                    <p>
                        من خلال خدماتنا الرقمية، نساعدك على بناء حضور أقوى
                        وتجربة أفضل في العالم الرقمي.
                    </p>
                    <a class="primary-btn" href="#services">
                        اكتشف خدماتنا
                        <span>←</span>
                    </a>
                </div>

                <div class="hero-visual">
                    <div class="hero-glow"></div>

                    <div class="hero-logo">
                        <svg class="mark" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 76C17 72 18 67 20 62L48 15C52 8 60 7 64 14L82 46C85 52 82 59 76 61L61 66L48 43L37 62L31 75C29 80 23 81 20 76Z" fill="#075CFF"/>
                            <path d="M47 43L61 66L82 46L64 14C61 9 55 10 51 16L39 37L47 43Z" fill="#0A3B91"/>
                            <path d="M37 62L47 43L61 66L44 78C38 82 32 75 37 62Z" fill="#16B8EF"/>
                        </svg>
                        <h2>anasit</h2>
                        <p>أنس لتكنولوجيا المعلومات</p>
                    </div>

                    <div class="laptop"></div>
                </div>

            </div>
        </div>
    </section>

    <section class="section" id="services">
        <div class="container">
            <div class="section-heading">
                <h2>خدماتنا</h2>
                <p>مجموعة من الخدمات الرقمية المصممة لتسهيل عملك ومساعدتك على النمو.</p>
            </div>

            <div class="services">

                <article class="service">
                    <div class="service-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7">
                            <rect x="3" y="5" width="18" height="14" rx="2"/>
                            <path d="m3 7 9 6 9-6"/>
                        </svg>
                    </div>
                    <h3>البريد الإلكتروني للشركات</h3>
                    <p>
                        احصل على بريد إلكتروني احترافي باسم شركتك
                        لزيادة الثقة والمصداقية.
                    </p>
                    <a class="service-link" href="#contact">المزيد ←</a>
                </article>

                <article class="service">
                    <div class="service-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7">
                            <path d="M7 18a5 5 0 1 1 1.5-9.77A6 6 0 0 1 20 11.5a4.5 4.5 0 0 1-1 8.88H7"/>
                        </svg>
                    </div>
                    <h3>خدمات SaaS</h3>
                    <p>
                        استفد من أحدث الحلول البرمجية كخدمة
                        لبناء أعمالك بكفاءة ومرونة.
                    </p>
                    <a class="service-link" href="#contact">المزيد ←</a>
                </article>

                <article class="service">
                    <div class="service-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7">
                            <path d="M3 4h2l2.2 10.5a2 2 0 0 0 2 1.5h7.9a2 2 0 0 0 1.9-1.4L21 8H7"/>
                            <circle cx="10" cy="20" r="1.2"/>
                            <circle cx="18" cy="20" r="1.2"/>
                        </svg>
                    </div>
                    <h3>Shatir.click</h3>
                    <p>
                        أنشئ متجرك الإلكتروني بسهولة مع Shatir
                        وابدأ رحلتك في التجارة الرقمية.
                    </p>
                    <a class="service-link" href="https://shatir.click" target="_blank" rel="noopener">زيارة Shatir ←</a>
                </article>

            </div>
        </div>
    </section>

    <section class="section about" id="about">
        <div class="container about-grid">

            <div>
                <h2>التقنية لخدمة أعمالك</h2>
                <p>
                    في <strong>anasit</strong> نعمل على تقديم حلول تقنية عملية
                    تساعد الشركات والأفراد على الاستفادة من التكنولوجيا
                    دون تعقيد.
                </p>
                <br>
                <p>
                    نبدأ بخدمات رقمية متخصصة، ونتوسع تدريجيًا نحو حلول
                    SaaS ومنتجات تقنية تخدم احتياجات السوق.
                </p>
            </div>

            <div>
                <div class="about-card">
                    <strong>حلول عملية</strong>
                    <p>نركز على المنتجات والخدمات التي تضيف قيمة حقيقية للأعمال.</p>
                </div>
                <div class="about-card">
                    <strong>بنية قابلة للتوسع</strong>
                    <p>حلول تبدأ بسيطة ويمكن تطويرها مع نمو احتياجاتك.</p>
                </div>
                <div class="about-card">
                    <strong>مستقبل رقمي أفضل</strong>
                    <p>نحوّل الأفكار إلى خدمات ومنتجات رقمية قابلة للاستخدام.</p>
                </div>
            </div>

        </div>
    </section>

    <section class="section contact" id="contact">
        <div class="container">
            <div class="contact-box">
                <h2>هل لديك فكرة أو مشروع؟</h2>
                <p>تواصل معنا لنتحدث عن الحل المناسب لعملك.</p>
                <a class="primary-btn" href="mailto:info@anasit.clude">تواصل معنا ←</a>
            </div>
        </div>
    </section>

</main>

<footer>
    <div class="footer-logo">anasit</div>
    <p>أنس لتكنولوجيا المعلومات</p>
    <p>حلول تقنية متكاملة لنمو أعمالك</p>

    <div class="copyright">
        © <?= date('Y') ?> anasit. جميع الحقوق محفوظة.
    </div>
</footer>

</body>
</html>
