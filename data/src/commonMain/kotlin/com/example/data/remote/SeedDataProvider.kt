package com.example.data.remote

import com.example.data.remote.dto.*
import kotlinx.datetime.Instant

object SeedDataProvider {

    // Seed Users
    val userAlice = UserDto(
        uid = "user_a",
        email = "alice@example.com",
        name = "Alice Adams",
        handle = "alice_adams",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
        accuracy = 45,
        reputation = 120,
        resolvedPredictionCount = 4 // Threshold Boundary: Below 5 (accuracy hidden)
    )

    val userBob = UserDto(
        uid = "user_b",
        email = "bob@example.com",
        name = "Bob Builder",
        handle = "bob_b",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
        accuracy = 72,
        reputation = 450,
        resolvedPredictionCount = 5 // Threshold Boundary: Exactly 5 (accuracy shown)
    )

    val userEstablished = UserDto(
        uid = "user_c",
        email = "established@example.com",
        name = "Eugene Fan",
        handle = "eugene_fan",
        avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6",
        accuracy = 80,
        reputation = 1200,
        resolvedPredictionCount = 12 // Established Account: 5+ (accuracy shown)
    )

    val userNew = UserDto(
        uid = "user_new",
        email = "new@example.com",
        name = "New Predictor",
        handle = "new_p",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9",
        accuracy = 0,
        reputation = 0,
        resolvedPredictionCount = 0 // New Authenticated: 0 resolved (accuracy hidden)
    )

    fun getDefaultPredictions(): List<PredictionDto> {
        return listOf(
            // 2-Outcome, Live, with heroImageUrl, Politics
            PredictionDto(
                id = "pred_2_outcomes",
                category = "POLITICS",
                status = "LIVE",
                publishStatus = "APPROVED",
                title = "Will the UK rejoin the EU single market by 2030?",
                description = "This prediction covers whether the UK government will sign an agreement rejoining the single market.",
                rulesDescription = "Must be official government policy/agreement signed by December 31, 2030.",
                heroImageUrl = "https://images.unsplash.com/photo-1513635269975-59663e0ca1ad",
                outcomeImagesSource = "PREDEFINED",
                createdAt = Instant.parse("2026-07-01T00:00:00Z"),
                closesAt = Instant.parse("2026-12-31T23:59:59Z"),
                resolvesAt = Instant.parse("2027-01-15T00:00:00Z"),
                totalSeconds = 450000,
                options = listOf(
                    OptionDto("opt_2_yes", "Yes, rejoin", 270000, 60, "SAGE", "https://images.unsplash.com/photo-1507608869274-d3177c8bb4c7"),
                    OptionDto("opt_2_no", "No, remain out", 180000, 40, "ORANGE", "https://images.unsplash.com/photo-1513635269975-59663e0ca1ad")
                ),
                resolutionSource = "UK Government Official Statements",
                createdBy = "user_a",
                approvedBy = "admin"
            ),
            // 3-Outcome, Live, without heroImageUrl, Technology
            PredictionDto(
                id = "pred_3_outcomes",
                category = "TECHNOLOGY",
                status = "LIVE",
                publishStatus = "APPROVED",
                title = "Most Popular Language on GitHub in 2027",
                description = "Which programming language will occupy the top slot in the annual Octoverse report?",
                rulesDescription = "Determined by GitHub's official annual report in late 2027.",
                heroImageUrl = null,
                outcomeImagesSource = null,
                createdAt = Instant.parse("2026-07-02T00:00:00Z"),
                closesAt = Instant.parse("2027-11-01T00:00:00Z"),
                resolvesAt = Instant.parse("2027-11-15T00:00:00Z"),
                totalSeconds = 120000,
                options = listOf(
                    OptionDto("opt_3_py", "Python", 60000, 50, "BLUE"),
                    OptionDto("opt_3_ts", "TypeScript", 36000, 30, "SAGE"),
                    OptionDto("opt_3_rs", "Rust", 24000, 20, "PURPLE")
                ),
                resolutionSource = "GitHub Octoverse State of the Octoverse Report",
                createdBy = "user_b",
                approvedBy = "admin"
            ),
            // 4-Outcome, Live, with heroImageUrl, Sports
            PredictionDto(
                id = "pred_4_outcomes",
                category = "SPORTS",
                status = "LIVE",
                publishStatus = "APPROVED",
                title = "Who will win the 2026 World Cup?",
                description = "Predicting the champion country of the upcoming football tournament.",
                rulesDescription = "Winner of the final match of the 2026 World Cup.",
                heroImageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2",
                outcomeImagesSource = "PREDEFINED",
                createdAt = Instant.parse("2026-06-15T00:00:00Z"),
                closesAt = Instant.parse("2026-07-18T00:00:00Z"),
                resolvesAt = Instant.parse("2026-07-20T00:00:00Z"),
                totalSeconds = 850000,
                options = listOf(
                    OptionDto("opt_4_br", "Brazil", 340000, 40, "AMBER", "https://images.unsplash.com/photo-1484549638198-a78a5903a86e"),
                    OptionDto("opt_4_fr", "France", 255000, 30, "BLUE", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34"),
                    OptionDto("opt_4_ar", "Argentina", 170000, 20, "TEAL", "https://images.unsplash.com/photo-1518005020951-eccb494ad742"),
                    OptionDto("opt_4_en", "England", 85000, 10, "ORANGE", "https://images.unsplash.com/photo-1513635269975-59663e0ca1ad")
                ),
                resolutionSource = "FIFA Official Match Results",
                createdBy = "user_c",
                approvedBy = "admin"
            ),
            // 5-Outcome, Live, without heroImageUrl, Economy
            PredictionDto(
                id = "pred_5_outcomes",
                category = "ECONOMY",
                status = "LIVE",
                publishStatus = "APPROVED",
                title = "US Federal Reserve Interest Rate in Dec 2026",
                description = "What range will the Fed's benchmark target interest rate occupy?",
                rulesDescription = "Determined by the FOMC meeting press release in December 2026.",
                heroImageUrl = null,
                outcomeImagesSource = null,
                createdAt = Instant.parse("2026-07-03T00:00:00Z"),
                closesAt = Instant.parse("2026-12-10T00:00:00Z"),
                resolvesAt = Instant.parse("2026-12-15T00:00:00Z"),
                totalSeconds = 300000,
                options = listOf(
                    OptionDto("opt_5_under4", "< 4.00%", 45000, 15, "TEAL"),
                    OptionDto("opt_5_4to425", "4.00% - 4.25%", 75000, 25, "SAGE"),
                    OptionDto("opt_5_425to45", "4.26% - 4.50%", 105000, 35, "BLUE"),
                    OptionDto("opt_5_45to475", "4.51% - 4.75%", 60000, 20, "AMBER"),
                    OptionDto("opt_5_over475", "> 4.75%", 15000, 5, "ORANGE")
                ),
                resolutionSource = "Federal Reserve Board FOMC Decisions",
                createdBy = "user_b",
                approvedBy = "admin"
            ),
            // 6-Outcome, Live, with heroImageUrl, Science
            // Includes long outcome names to test truncation/collapse rules
            PredictionDto(
                id = "pred_6_outcomes",
                category = "SCIENCE",
                status = "LIVE",
                publishStatus = "APPROVED",
                title = "Which entity lands humans on Mars first?",
                description = "First successful human landing on Martian surface.",
                rulesDescription = "Must be officially confirmed by a crewed capsule touching down and safely exiting.",
                heroImageUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9",
                outcomeImagesSource = "PREDEFINED",
                createdAt = Instant.parse("2026-07-04T00:00:00Z"),
                closesAt = Instant.parse("2035-12-31T00:00:00Z"),
                resolvesAt = Instant.parse("2036-01-15T00:00:00Z"),
                totalSeconds = 999999,
                options = listOf(
                    OptionDto("opt_6_spacex", "SpaceX (with Starship Program)", 499999, 50, "PURPLE", "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2"),
                    OptionDto("opt_6_nasa", "NASA ( Artemis / Mars Coalition)", 199999, 20, "BLUE", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa"),
                    OptionDto("opt_6_cnsa", "CNSA (China National Space Administration)", 149999, 15, "ORANGE", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86"),
                    OptionDto("opt_6_esa", "ESA (European Space Agency)", 49999, 5, "SAGE", "https://images.unsplash.com/photo-1451187580459-43490279c0fa"),
                    OptionDto("opt_6_isro", "ISRO (Indian Space Research Organisation)", 49999, 5, "TEAL", "https://images.unsplash.com/photo-1502134249126-9f3755a50d78"),
                    OptionDto("opt_6_none", "None of these before the year 2045", 49999, 5, "AMBER", "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9")
                ),
                resolutionSource = "Space Agencies Statements & Major Publications",
                createdBy = "user_c",
                approvedBy = "admin"
            ),
            // Prediction with raw snapshots but no downsampled rollup yet
            PredictionDto(
                id = "pred_no_rollup",
                category = "BUSINESS",
                status = "LIVE",
                publishStatus = "APPROVED",
                title = "Will Apple acquire a major film studio by late 2026?",
                description = "Covers any purchase exceeding $5B value.",
                rulesDescription = "Determined by SEC filings or official company press announcements.",
                heroImageUrl = "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5",
                outcomeImagesSource = null,
                createdAt = Instant.parse("2026-07-05T00:00:00Z"),
                closesAt = Instant.parse("2026-12-15T00:00:00Z"),
                resolvesAt = Instant.parse("2026-12-30T00:00:00Z"),
                totalSeconds = 150000,
                options = listOf(
                    OptionDto("opt_noroll_yes", "Yes, studio acquired", 30000, 20, "SAGE"),
                    OptionDto("opt_noroll_no", "No acquisition", 120000, 80, "ORANGE")
                ),
                resolutionSource = "SEC Filings",
                createdBy = "user_c",
                approvedBy = "admin"
            )
        )
    }

    // Default comments
    fun getDefaultComments(): List<CommentDto> {
        return listOf(
            CommentDto(
                id = "comm_1",
                predictionId = "pred_2_outcomes",
                authorUid = "user_a",
                authorName = "Alice Adams",
                authorHandle = "alice_adams",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                secondedOptionId = "opt_2_yes",
                postedAt = Instant.parse("2026-07-05T12:00:00Z"),
                text = "The trade figures and political alignment indicate rejoining the single market is economically necessary.",
                helpfulCount = 15,
                lockedAt = null,
                flagCount = 0
            ),
            CommentDto(
                id = "comm_2",
                predictionId = "pred_2_outcomes",
                authorUid = "user_b",
                authorName = "Bob Builder",
                authorHandle = "bob_b",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                secondedOptionId = "opt_2_no",
                postedAt = Instant.parse("2026-07-06T14:30:00Z"),
                text = "Domestic political consensus remains heavily opposed to rejoining, regardless of trade pressures.",
                helpfulCount = 8,
                lockedAt = null,
                flagCount = 0
            ),
            // Moderation Card: Reasoning Flagged (has high flags, e.g. 50 flags)
            CommentDto(
                id = "comm_flagged_reasoning",
                predictionId = "pred_2_outcomes",
                authorUid = "user_new",
                authorName = "Spam User",
                authorHandle = "spam_u",
                authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9",
                secondedOptionId = "opt_2_no",
                postedAt = Instant.parse("2026-07-06T18:00:00Z"),
                text = "[REMOVED FOR VIOLATING DISCOURSE RULES] Buy crypto now at fake-link-spam.com!!!",
                helpfulCount = 0,
                lockedAt = Instant.parse("2026-07-06T20:00:00Z"),
                flagCount = 50 // Exactly 50 flags: Boundary check
            )
        )
    }

    // Default discourse entries with 3-levels deep threading
    fun getDefaultDiscourse(): List<DiscourseDto> {
        return listOf(
            // Level 1
            DiscourseDto(
                id = "disc_1",
                predictionId = "pred_6_outcomes",
                authorUid = "user_a",
                authorName = "Alice Adams",
                authorHandle = "alice_adams",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                postedAt = Instant.parse("2026-07-05T10:00:00Z"),
                text = "I think SpaceX is definitely leading because of the development speed of Starship.",
                parentId = null,
                helpfulCount = 20,
                flagCount = 2
            ),
            // Level 2 (reply to Level 1)
            DiscourseDto(
                id = "disc_2",
                predictionId = "pred_6_outcomes",
                authorUid = "user_b",
                authorName = "Bob Builder",
                authorHandle = "bob_b",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                postedAt = Instant.parse("2026-07-05T10:30:00Z"),
                text = "Starship still has to solve the massive orbital refuelling problem first though.",
                parentId = "disc_1",
                helpfulCount = 14,
                flagCount = 0
            ),
            // Level 3 (reply to Level 2)
            DiscourseDto(
                id = "disc_3",
                predictionId = "pred_6_outcomes",
                authorUid = "user_c",
                authorName = "Eugene Fan",
                authorHandle = "eugene_fan",
                authorAvatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6",
                postedAt = Instant.parse("2026-07-05T11:00:00Z"),
                text = "True, but their rapid iteration cycle and flight test frequency makes it highly likely they will solve it sooner than NASA's SLS pathway.",
                parentId = "disc_2",
                helpfulCount = 35,
                flagCount = 1
            )
        )
    }

    // Default KeepTabs: tracker user established (user_c) tracking alice (user_a)
    // Alice is under the 5-resolved-prediction threshold (she has 4), checking no-accuracy-gate rule.
    fun getDefaultKeepTabs(): List<KeepTabDto> {
        return listOf(
            KeepTabDto(
                id = "tab_1",
                trackerUid = "user_c",
                trackedUid = "user_a",
                createdAt = Instant.parse("2026-07-05T12:00:00Z")
            )
        )
    }

    private fun makeSnapshots(
        predictionId: String,
        optionIds: List<String>,
        basePercentages: List<Int>,
        pointCount: Int = 30
    ): List<SecondingSnapshotDto> {
        val baseInstant = Instant.parse("2026-07-16T15:00:00Z")
        val result = mutableListOf<SecondingSnapshotDto>()
        
        for (i in 0 until pointCount) {
            val timestamp = Instant.fromEpochMilliseconds(baseInstant.toEpochMilliseconds() - (pointCount - 1 - i) * 4 * 60 * 60 * 1000L)
            val rawOffsets = basePercentages.mapIndexed { index, baseValue ->
                val sineValue = kotlin.math.sin((i + index * 3).toDouble() * 0.4) * 10.0
                (baseValue + sineValue).toInt().coerceIn(5, 95)
            }
            val totalRaw = rawOffsets.sum()
            val normalizedPercentages = rawOffsets.map { (it * 100) / totalRaw }
            val difference = 100 - normalizedPercentages.sum()
            val finalPercentages = normalizedPercentages.mapIndexed { index, p ->
                if (index == 0) p + difference else p
            }
            val totalSeconds = 1000 + i * 300
            val outcomes = optionIds.mapIndexed { index, optionId ->
                val percentage = finalPercentages[index]
                val seconds = (totalSeconds * percentage) / 100
                SecondingSnapshotOutcomeDto(optionId, percentage, seconds)
            }
            result.add(
                SecondingSnapshotDto(
                    predictionId = predictionId,
                    timestamp = timestamp,
                    totalSeconds = totalSeconds,
                    outcomes = outcomes
                )
            )
        }
        return result
    }

    // Snapshots for prediction with raw history but no downsampled rollup yet
    fun getDefaultSnapshots(): List<SecondingSnapshotDto> {
        val list = mutableListOf<SecondingSnapshotDto>()
        list.addAll(makeSnapshots("pred_2_outcomes", listOf("opt_2_yes", "opt_2_no"), listOf(60, 40)))
        list.addAll(makeSnapshots("pred_3_outcomes", listOf("opt_3_py", "opt_3_ts", "opt_3_rs"), listOf(50, 30, 20)))
        list.addAll(makeSnapshots("pred_4_outcomes", listOf("opt_4_br", "opt_4_fr", "opt_4_ar", "opt_4_en"), listOf(40, 30, 20, 10)))
        list.addAll(makeSnapshots("pred_5_outcomes", listOf("opt_5_under4", "opt_5_4to425", "opt_5_425to45", "opt_5_45to475", "opt_5_over475"), listOf(15, 25, 35, 20, 5)))
        list.addAll(makeSnapshots("pred_6_outcomes", listOf("opt_6_spacex", "opt_6_nasa", "opt_6_cnsa", "opt_6_esa", "opt_6_isro", "opt_6_none"), listOf(50, 20, 15, 5, 5, 5)))
        list.addAll(makeSnapshots("pred_no_rollup", listOf("opt_noroll_yes", "opt_noroll_no"), listOf(20, 80)))
        list.addAll(makeSnapshots("pred_content_removed", listOf("opt_rem_1", "opt_rem_2"), listOf(50, 50)))
        list.addAll(makeSnapshots("pred_flags_199", listOf("opt_acme_yes", "opt_acme_no"), listOf(80, 20)))
        list.addAll(makeSnapshots("pred_flags_200", listOf("opt_acme2_yes", "opt_acme2_no"), listOf(80, 20)))
        return list
    }

    // Submissions
    fun getDefaultSubmissions(): List<Map<String, String>> {
        return listOf(
            mapOf(
                "id" to "sub_1",
                "title" to "Will the USD remain the global reserve currency?",
                "category" to "ECONOMY",
                "submittedBy" to "user_a",
                "submittedAt" to "2026-07-10T12:00:00Z",
                "closesAt" to "2028-12-31T00:00:00Z",
                "resolvesAt" to "2029-01-15T00:00:00Z",
                "source" to "IMF Reports",
                "criteria" to "IMF official global asset percentages list.",
                "state" to "SUBMITTED"
            ),
            mapOf(
                "id" to "sub_2",
                "title" to "First commercial fusion reactor online before 2035",
                "category" to "SCIENCE",
                "submittedBy" to "user_b",
                "submittedAt" to "2026-07-11T12:00:00Z",
                "closesAt" to "2034-12-31T00:00:00Z",
                "resolvesAt" to "2035-01-15T00:00:00Z",
                "source" to "ITER/IAEA Publications",
                "criteria" to "ITER fusion yield announcements.",
                "state" to "APPROVED"
            )
        )
    }

    // Edge Cases Seed Fixtures (added on-demand via "Seed Edge Cases" debug trigger)
    fun getEdgeCasePredictions(): List<PredictionDto> {
        return listOf(
            // Moderation Card: Content Removed (A prediction flagged heavily and rejected or voided)
            PredictionDto(
                id = "pred_content_removed",
                category = "CULTURE",
                status = "REJECTED",
                publishStatus = "REJECTED",
                title = "[CONTENT REMOVED BY MODERATOR]",
                description = "This description was flagged and removed due to severe terms-of-service violations.",
                rulesDescription = "No rules available.",
                heroImageUrl = null,
                outcomeImagesSource = null,
                createdAt = Instant.parse("2026-07-15T10:00:00Z"),
                closesAt = Instant.parse("2026-07-20T10:00:00Z"),
                resolvesAt = Instant.parse("2026-07-21T10:00:00Z"),
                totalSeconds = 0,
                options = listOf(
                    OptionDto("opt_rem_1", "Removed Option A", 0, 50, "SAGE"),
                    OptionDto("opt_rem_2", "Removed Option B", 0, 50, "ORANGE")
                ),
                resolutionSource = "N/A",
                createdBy = "user_new",
                flagCount = 500, // Severe: boundary flag count
                rejectionReason = "Inappropriate and abusive language."
            ),
            // Prediction at exact 199 flags on resolution (under re-review threshold)
            PredictionDto(
                id = "pred_flags_199",
                category = "BUSINESS",
                status = "RESOLVED",
                publishStatus = "APPROVED",
                title = "Will Acme Corp go public in 2026? (199 Flags)",
                description = "Checking boundary flags under 200 re-review threshold.",
                rulesDescription = "Must go public on NYSE or NASDAQ.",
                heroImageUrl = null,
                outcomeImagesSource = null,
                createdAt = Instant.parse("2026-07-10T12:00:00Z"),
                closesAt = Instant.parse("2026-07-12T12:00:00Z"),
                resolvesAt = Instant.parse("2026-07-15T12:00:00Z"),
                totalSeconds = 10000,
                options = listOf(
                    OptionDto("opt_acme_yes", "Yes, public", 8000, 80, "SAGE"),
                    OptionDto("opt_acme_no", "No, private", 2000, 20, "ORANGE")
                ),
                resolutionSource = "SEC",
                createdBy = "user_c",
                approvedBy = "admin",
                flagCount = 199, // 199 Flags: Under 200 threshold
                resolvedOutcomeId = "opt_acme_yes"
            ),
            // Prediction at exact 200 flags on resolution (re-review threshold boundary)
            PredictionDto(
                id = "pred_flags_200",
                category = "BUSINESS",
                status = "RESOLVED",
                publishStatus = "APPROVED",
                title = "Will Acme Corp go public in 2026? (200 Flags)",
                description = "Checking boundary flags at exact 200 re-review threshold.",
                rulesDescription = "Must go public on NYSE or NASDAQ.",
                heroImageUrl = null,
                outcomeImagesSource = null,
                createdAt = Instant.parse("2026-07-10T12:00:00Z"),
                closesAt = Instant.parse("2026-07-12T12:00:00Z"),
                resolvesAt = Instant.parse("2026-07-15T12:00:00Z"),
                totalSeconds = 10000,
                options = listOf(
                    OptionDto("opt_acme2_yes", "Yes, public", 8000, 80, "SAGE"),
                    OptionDto("opt_acme2_no", "No, private", 2000, 20, "ORANGE")
                ),
                resolutionSource = "SEC",
                createdBy = "user_c",
                approvedBy = "admin",
                flagCount = 200, // 200 Flags: Re-review boundary
                resolvedOutcomeId = "opt_acme2_yes"
            )
        )
    }

    // Moderation: Account Limited
    fun getAccountLimitedUser(): UserDto {
        return UserDto(
            uid = "user_limited",
            email = "limited@example.com",
            name = "Flagged User [LIMITED]",
            handle = "spam_limited",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9",
            accuracy = 0,
            reputation = -100, // Negative reputation indicating system-limited status
            resolvedPredictionCount = 2
        )
    }
}
