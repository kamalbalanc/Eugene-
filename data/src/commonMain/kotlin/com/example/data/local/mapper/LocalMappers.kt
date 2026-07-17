package com.example.data.local.mapper

import com.example.data.database.entity.*
import com.example.domain.model.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PredictionOptionLocalDto(
    val id: String,
    val text: String,
    val seconds: Int,
    val percentage: Int,
    val accent: String,
    val imageUrl: String? = null
)

fun PredictionOption.toLocalDto(): PredictionOptionLocalDto {
    return PredictionOptionLocalDto(
        id = id,
        text = text,
        seconds = seconds,
        percentage = percentage,
        accent = accent.name,
        imageUrl = imageUrl
    )
}

fun PredictionOptionLocalDto.toDomain(): PredictionOption {
    return PredictionOption(
        id = id,
        text = text,
        seconds = seconds,
        percentage = percentage,
        accent = try { PredictionAccent.valueOf(accent.uppercase()) } catch (e: Exception) { PredictionAccent.SAGE },
        imageUrl = imageUrl
    )
}

fun PredictionRecord.toDomain(): Prediction {
    val decodedOptions = try {
        Json.decodeFromString<List<PredictionOptionLocalDto>>(optionsJson)
    } catch (e: Exception) {
        emptyList()
    }
    return Prediction(
        id = id,
        category = try { PredictionCategory.valueOf(category.uppercase()) } catch (e: Exception) { PredictionCategory.POLITICS },
        status = status?.let { try { PredictionStatus.valueOf(it.uppercase()) } catch (e: Exception) { PredictionStatus.PENDING } } ?: PredictionStatus.PENDING,
        publishStatus = try { PredictionPublishStatus.valueOf(publishStatus.uppercase()) } catch (e: Exception) { PredictionPublishStatus.PENDING },
        title = title,
        description = description,
        rulesDescription = rulesDescription,
        heroImageUrl = heroImageUrl,
        outcomeImagesSource = outcomeImagesSource?.let { try { ImageSource.valueOf(it.uppercase()) } catch (e: Exception) { null } },
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
        closesAt = Instant.fromEpochMilliseconds(closesAtEpochMs),
        resolvesAt = Instant.fromEpochMilliseconds(resolvesAtEpochMs),
        totalSeconds = totalSeconds,
        options = decodedOptions.map { it.toDomain() },
        resolutionSource = resolutionSource,
        createdBy = createdBy,
        approvedBy = approvedBy,
        flagCount = flagCount,
        resolvedOutcomeId = resolvedOutcomeId,
        resolutionSourceURL = resolutionSourceURL,
        voidReason = voidReason,
        rejectionReason = rejectionReason
    )
}

fun Prediction.toRecord(): PredictionRecord {
    val encodedOptionsJson = Json.encodeToString(options.map { it.toLocalDto() })
    return PredictionRecord(
        id = id,
        category = category.name,
        status = status.name,
        publishStatus = publishStatus.name,
        title = title,
        description = description,
        rulesDescription = rulesDescription,
        heroImageUrl = heroImageUrl,
        outcomeImagesSource = outcomeImagesSource?.name,
        createdAtEpochMs = createdAt.toEpochMilliseconds(),
        closesAtEpochMs = closesAt.toEpochMilliseconds(),
        resolvesAtEpochMs = resolvesAt.toEpochMilliseconds(),
        totalSeconds = totalSeconds,
        optionsJson = encodedOptionsJson,
        resolutionSource = resolutionSource,
        createdBy = createdBy,
        approvedBy = approvedBy,
        flagCount = flagCount,
        resolvedOutcomeId = resolvedOutcomeId,
        resolutionSourceURL = resolutionSourceURL,
        voidReason = voidReason,
        rejectionReason = rejectionReason
    )
}

fun SecondRecord.toDomain(): Second {
    return Second(
        id = id,
        predictionId = predictionId,
        optionId = optionId,
        reasoning = reasoning,
        castAt = Instant.fromEpochMilliseconds(castAtEpochMs),
        status = try { SecondStatus.valueOf(status.uppercase()) } catch (e: Exception) { SecondStatus.PENDING },
        crowdPercentAtSecond = crowdPercentAtSecond,
        reasoningLockedAt = reasoningLockedAtEpochMs?.let { Instant.fromEpochMilliseconds(it) },
        secondLockedAt = secondLockedAtEpochMs?.let { Instant.fromEpochMilliseconds(it) }
    )
}

fun Second.toRecord(): SecondRecord {
    return SecondRecord(
        id = id,
        predictionId = predictionId,
        optionId = optionId,
        reasoning = reasoning,
        castAtEpochMs = castAt.toEpochMilliseconds(),
        status = status.name,
        crowdPercentAtSecond = crowdPercentAtSecond,
        reasoningLockedAtEpochMs = reasoningLockedAt?.toEpochMilliseconds(),
        secondLockedAtEpochMs = secondLockedAt?.toEpochMilliseconds()
    )
}

fun CommentRecord.toDomain(): Comment {
    return Comment(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        secondedOptionId = secondedOptionId,
        postedAt = Instant.fromEpochMilliseconds(postedAtEpochMs),
        text = text,
        helpfulCount = helpfulCount,
        lockedAt = lockedAtEpochMs?.let { Instant.fromEpochMilliseconds(it) },
        flagCount = flagCount
    )
}

fun Comment.toRecord(): CommentRecord {
    return CommentRecord(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        secondedOptionId = secondedOptionId,
        postedAtEpochMs = postedAt.toEpochMilliseconds(),
        text = text,
        helpfulCount = helpfulCount,
        lockedAtEpochMs = lockedAt?.toEpochMilliseconds(),
        flagCount = flagCount
    )
}

fun DiscourseRecord.toDomain(): DiscourseEntry {
    return DiscourseEntry(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        postedAt = Instant.fromEpochMilliseconds(postedAtEpochMs),
        text = text,
        parentId = parentId,
        helpfulCount = helpfulCount,
        flagCount = flagCount
    )
}

fun DiscourseEntry.toRecord(): DiscourseRecord {
    return DiscourseRecord(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        postedAtEpochMs = postedAt.toEpochMilliseconds(),
        text = text,
        parentId = parentId,
        helpfulCount = helpfulCount,
        flagCount = flagCount
    )
}

fun KeepTabRecord.toDomain(): KeepTab {
    return KeepTab(
        id = id,
        trackerUid = trackerUid,
        trackedUid = trackedUid,
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs)
    )
}

fun KeepTab.toRecord(): KeepTabRecord {
    return KeepTabRecord(
        id = id,
        trackerUid = trackerUid,
        trackedUid = trackedUid,
        createdAtEpochMs = createdAt.toEpochMilliseconds()
    )
}

fun SubmissionRecord.toDomain(): Submission {
    val decodedOutcomes = try {
        Json.decodeFromString<List<String>>(outcomesJson)
    } catch (e: Exception) {
        emptyList()
    }
    return Submission(
        id = id,
        title = title,
        category = try { PredictionCategory.valueOf(category.uppercase()) } catch (e: Exception) { PredictionCategory.POLITICS },
        submittedBy = submittedBy,
        submittedAt = Instant.fromEpochMilliseconds(submittedAtEpochMs),
        closesAt = Instant.fromEpochMilliseconds(closesAtEpochMs),
        resolvesAt = Instant.fromEpochMilliseconds(resolvesAtEpochMs),
        source = source,
        criteria = criteria,
        state = try { ApprovalState.valueOf(state.uppercase()) } catch (e: Exception) { ApprovalState.SUBMITTED },
        outcomes = decodedOutcomes
    )
}

fun Submission.toRecord(): SubmissionRecord {
    val encodedOutcomesJson = Json.encodeToString(outcomes)
    return SubmissionRecord(
        id = id,
        title = title,
        category = category.name,
        submittedBy = submittedBy,
        submittedAtEpochMs = submittedAt.toEpochMilliseconds(),
        closesAtEpochMs = closesAt.toEpochMilliseconds(),
        resolvesAtEpochMs = resolvesAt.toEpochMilliseconds(),
        source = source,
        criteria = criteria,
        state = state.name,
        outcomesJson = encodedOutcomesJson
    )
}

fun SecondingSnapshot.toRecord(): SecondingSnapshotRecord {
    return SecondingSnapshotRecord(
        predictionId = predictionId,
        timestamp = timestamp.toEpochMilliseconds(),
        totalSeconds = totalSeconds
    )
}

fun SecondingSnapshot.toOutcomeRecords(): List<SecondingSnapshotOutcomeRecord> {
    val ts = timestamp.toEpochMilliseconds()
    return outcomes.map {
        SecondingSnapshotOutcomeRecord(
            predictionId = predictionId,
            timestamp = ts,
            outcomeId = it.outcomeId,
            percentage = it.percentage,
            seconds = it.seconds
        )
    }
}

fun mapSnapshot(record: SecondingSnapshotRecord, outcomes: List<SecondingSnapshotOutcomeRecord>): SecondingSnapshot {
    return SecondingSnapshot(
        predictionId = record.predictionId,
        timestamp = Instant.fromEpochMilliseconds(record.timestamp),
        totalSeconds = record.totalSeconds,
        outcomes = outcomes.map {
            SecondingSnapshotOutcome(
                outcomeId = it.outcomeId,
                percentage = it.percentage,
                seconds = it.seconds
            )
        }
    )
}
