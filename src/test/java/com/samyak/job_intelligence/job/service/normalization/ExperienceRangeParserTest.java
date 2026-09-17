package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.service.parsing.ExperienceParser;
import com.samyak.job_intelligence.job.service.parsing.ExperienceRange;

import com.samyak.job_intelligence.llm.JobDescriptionCleaner;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceRangeParserTest {

    private final ExperienceParser parser = new ExperienceParser();

    @Test
    void shouldParseMinimumExperienceFromAirbnbJobDescription() {

        String description = """
                <div class="content-intro">
                <p><span style="font-family: helvetica, arial, sans-serif; font-size: 12pt;">
                Airbnb was born in 2007 when two hosts welcomed three guests to their San Francisco home,
                and has since grown to over 5 million hosts who have welcomed over 2 billion guest arrivals
                in almost every country across the globe. Every day, hosts offer unique stays and experiences
                that make it possible for guests to connect with communities in a more authentic way.
                </span></p>
                </div>
                <p><strong>The Community You Will Join:</strong></p>

                <p>
                The Chief Business Officer organization plays a pivotal role in driving the company’s growth
                and expansion efforts worldwide. Our teammates work on commercializing Airbnb’s new and
                existing businesses, helping Airbnb expand beyond its core offering as a place to stay.
                This includes recruiting and developing Airbnb’s global supply of high-quality stays,
                Experiences, and services, and executing Airbnb’s global in market strategy.
                With this focus on business strategy, quality supply and international expansion,
                the team is leading the way for Airbnb’s continued growth and facilitation of millions
                more host and guest connections.
                </p>

                <p><strong>The difference you will make:</strong></p>

                <p>
                The Airbnb Brazil team is looking for a
                <strong>Senior Community Growth Manager</strong>
                to launch and manage host growth programs in Brazil.
                These programs include the Co-Host Network, Superhost Ambassadors,
                and other programs in incubation. In this role, you will be part of the
                Community Powered Growth team within our Business organization.
                This team’s objective is to drive growth of Airbnb’s host population
                and leverage new product offerings to create additional opportunities
                for existing hosts. This role comes with hybrid program operations and
                scaled relationship management responsibilities. This role carries
                revenue and business performance goals.
                </p>

                <p>
                You will be responsible for designing playbooks, while adapting central
                frameworks to capture local market dynamics that will shape future
                iterations of our community programs. You will partner with our central
                commercial teams, community teams, sales operations and other cross
                functional partners to pilot new projects, tactics and/or initiatives
                that can be scaled into broader cross-functional programs.
                </p>

                <p>
                You will analyze program performance and insights to craft strategic plans
                to ensure the needs of our hosts, co-hosts, and the Airbnb business are supported.
                Your success in this role will be based on a variety of metrics that reflect
                your impact to the business. This includes the level of growth, retention,
                and success of the hosting business in Brazil.
                </p>

                <p>
                The ideal candidate is a strategic operator, analytical, strong content
                development skills, and adept at business planning for the long term while
                remaining nimble and adaptive to short term priorities. This includes
                relationship building and management within our host community.
                </p>

                <p><strong>A typical day:</strong></p>

                <p><strong>Program Launches:</strong></p>

                <ul>
                <li>Supporting recruitment and onboarding education for new co-hosts,
                Ambassadors, and other hosts in these programs</li>

                <li>Analyzing regional and territory business trends and outcomes
                to identify and scale growth opportunities</li>

                <li>Designing, piloting, and testing new levers that support host growth at scale</li>

                <li>Developing and maintaining a community forum to increase reach and
                feedback loops for program, product support and community support</li>

                <li>Execution of workshops, events, webinars, partnerships and other channels
                to build an integrated strategy</li>

                <li>Adapting central programs to support regional objectives</li>
                </ul>

                <p><strong>Community Management:</strong></p>

                <ul>
                <li>Design and operationalize effective email and scaled communication
                campaigns to support lifecycle engagement and education</li>

                <li>Serve as the primary point of contact for hosts in the Community Powered
                Growth programs. Advising and supporting them as they build their property
                management businesses</li>

                <li>Building sentiment and feedback channels to size and package insights
                for product and central programs</li>

                <li>Identify and pilot opportunities to increase overall host success</li>
                </ul>

                <p><strong>Business Strategy:</strong></p>

                <ul>
                <li>You will be responsible for revenue and host growth goals</li>
                <li>You will develop new programs and initiatives</li>
                <li>You will implement plans and evaluate overall efficacy to inform business initiatives</li>
                <li>You will lead through inspiration, influence, and our core value of belonging</li>
                </ul>

                <p><strong>Outcomes and Impact:</strong></p>

                <ul>
                <li>You will contribute meaningfully to a strategic outcome for Airbnb
                to unlock the next generation of hosts</li>

                <li>You will pioneer a new growth model and structure for Airbnb.
                This includes developing project plans, collaborating with cross-functional
                teams around the world, partnering with multiple stakeholders, and crafting
                mitigation plans to solution design</li>

                <li>You will support the development, execution and scale of pilots
                to accelerate evolving business verticals</li>

                <li>Act as a change agent and effectively utilize change management
                methodologies when deploying solutions and processes.</li>

                <li>Lead resource development for the community with engaging and high impact communications</li>

                <li>Develop deep knowledge of your assigned geographic markets and become
                the go-to source relating to local, trends and market dynamics for internal
                and external partners</li>

                <li>Support online and offline community events (webinars, meetups, and workshops)</li>
                </ul>

                <p><strong>Your expertise:</strong></p>

                <ul>
                <li>
                <strong>8+</strong> years experience in Sales, Business Operations,
                or Business Development in Tech, Real Estate/Property Management, or Travel
                </li>

                <li>You are strategic in your thinking; proven ability to drive growth
                and to own growth OKRs while exceeding goals.</li>

                <li>You think and act like an entrepreneur. You are resourceful and goal driven.</li>

                <li>Strong project management skills and demonstrated experience
                leading strategic programs and processes.</li>

                <li>Can forecast, model, and optimize growth tactics based on known
                and unknown performance</li>

                <li>You have sales or external relationship management experience
                across tech and consumer goods</li>

                <li>Ability to influence through persuasion, negotiation, and consensus building</li>

                <li>Ability to assess a problem, opportunity or business challenges quickly
                and make decisions based on the facts presented, creative problem solving,
                and previous experience.</li>

                <li>Excellent communication (written &amp; verbal) and interpersonal skills
                with strong business acumen that will enable you to earn trust at all levels</li>

                <li>Strong empathy for our community AND passion for revenue and growth</li>

                <li>Deep understanding of value drivers in recurring revenue business models</li>

                <li>Enthusiastic and creative leader with the ability to inspire others</li>

                <li>Ability to flourish with minimal guidance, be proactive,
                and handle uncertainty and ambiguity</li>

                <li>Expert with Excel (pivot tables, large data set analyses, and formulas),
                analytical tools (SQL), and methodologies like capacity planning,
                forecasting, general modeling for prioritization and decision making</li>

                <li>Comfortable with Google Docs, Salesforce CRM, and other technology
                solutions for driving supply acquisition, territory management and retention.</li>

                <li>Deep knowledge and experience using collaborative project management
                tools and processes (e.g. Asana, Airtable, and etc.).</li>

                <li>
                <strong>Fluency in English, Portuguese, and Spanish, spoken and written is required.</strong>
                </li>
                </ul>

                <p><strong>How We'll Take Care of You:</strong></p>

                <p>
                Our job titles may span more than one career level. The actual base pay
                is dependent upon many factors, such as: training, transferable skills,
                work experience, business needs and market demands.
                </p>

                <div class="pay-range">
                <span>R$22.000</span>
                <span>—</span>
                <span>R$27.500 BRL</span>
                </div>
                """;

        String cleanDesc = JobDescriptionCleaner.clean(description);
        ExperienceRange result = parser.parse(cleanDesc);

        assertNotNull(result);

        assertEquals(
                new BigDecimal("8"),
                result.minYears()
        );

        assertNull(result.maxYears());
    }
}