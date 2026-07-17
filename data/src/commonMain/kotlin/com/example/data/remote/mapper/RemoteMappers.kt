package com.example.data.remote.mapper

import com.example.domain.model.*
import com.example.data.remote.dto.*

fun PredictionDto.toDomain(): Prediction {
    return Prediction(
        id = id,
        category = try { PredictionCategory.valueOf(category.uppercase()) } catch (e: Exception) { PredictionCategory.POLITICS },
        status = try { PredictionStatus.valueOf(status.uppercase()) } catch (e: Exception) { PredictionStatus.PENDING },
        publishStatus = try { PredictionPublishStatus.valueOf(publishStatus.uppercase()) } catch (e: Exception) { PredictionPublishStatus.PENDING },
        title = title,
        description = description,
        rulesDescription = rulesDescription,
        heroImageUrl = heroImageUrl,
        outcomeImagesSource = outcomeImagesSource?.let {
            try { ImageSource.valueOf(it.uppercase()) } catch (e: Exception) { null }
        },
        createdAt = createdAt,
        closesAt = closesAt,
        resolvesAt = resolvesAt,
        totalSeconds = totalSeconds,
        options = options.map { it.toDomain() },
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

fun Prediction.toDto(): PredictionDto {
    return PredictionDto(
        id = id,
        category = category.name,
        status = status.name,
        publishStatus = publishStatus.name,
        title = title,
        description = description,
        rulesDescription = rulesDescription,
        heroImageUrl = heroImageUrl,
        outcomeImagesSource = outcomeImagesSource?.name,
        createdAt = createdAt,
        closesAt = closesAt,
        resolvesAt = resolvesAt,
        totalSeconds = totalSeconds,
        options = options.map { it.toDto() },
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

fun OptionDto.toDomain(): PredictionOption {
    return PredictionOption(
        id = id,
        text = text,
        seconds = seconds,
        percentage = percentage,
        accent = try { PredictionAccent.valueOf(accent.uppercase()) } catch (e: Exception) { PredictionAccent.SAGE },
        imageUrl = imageUrl
    )
}

fun PredictionOption.toDto(): OptionDto {
    return OptionDto(
        id = id,
        text = text,
        seconds = seconds,
        percentage = percentage,
        accent = accent.name,
        imageUrl = imageUrl
    )
}

fun SecondDto.toDomain(): Second {
    return Second(
        id = id,
        predictionId = predictionId,
        optionId = optionId,
        reasoning = reasoning,
        castAt = castAt,
        status = try { SecondStatus.valueOf(status.uppercase()) } catch (e: Exception) { SecondStatus.PENDING },
        crowdPercentAtSecond = crowdPercentAtSecond,
        reasoningLockedAt = reasoningLockedAt,
        secondLockedAt = secondLockedAt
    )
}

fun Second.toDto(): SecondDto {
    return SecondDto(
        id = id,
        predictionId = predictionId,
        optionId = optionId,
        reasoning = reasoning,
        castAt = castAt,
        status = status.name,
        crowdPercentAtSecond = crowdPercentAtSecond,
        reasoningLockedAt = reasoningLockedAt,
        secondLockedAt = secondLockedAt
    )
}

fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        secondedOptionId = secondedOptionId,
        postedAt = postedAt,
        text = text,
        helpfulCount = helpfulCount,
        lockedAt = lockedAt,
        flagCount = flagCount
    )
}

fun Comment.toDto(): CommentDto {
    return CommentDto(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        secondedOptionId = secondedOptionId,
        postedAt = postedAt,
        text = text,
        helpfulCount = helpfulCount,
        lockedAt = lockedAt,
        flagCount = flagCount
    )
}

fun DiscourseDto.toDomain(): DiscourseEntry {
    return DiscourseEntry(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        postedAt = postedAt,
        text = text,
        parentId = parentId,
        helpfulCount = helpfulCount,
        flagCount = flagCount
    )
}

fun DiscourseEntry.toDto(): DiscourseDto {
    return DiscourseDto(
        id = id,
        predictionId = predictionId,
        authorUid = authorUid,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        postedAt = postedAt,
        text = text,
        parentId = parentId,
        helpfulCount = helpfulCount,
        flagCount = flagCount
    )
}

fun KeepTabDto.toDomain(): KeepTab {
    return KeepTab(
        id = id,
        trackerUid = trackerUid,
        trackedUid = trackedUid,
        createdAt = createdAt
    )
}

fun KeepTab.toDto(): KeepTabDto {
    return KeepTabDto(
        id = id,
        trackerUid = trackerUid,
        trackedUid = trackedUid,
        createdAt = createdAt
    )
}

fun SecondingSnapshotDto.toDomain(): SecondingSnapshot {
    return SecondingSnapshot(
        predictionId = predictionId,
        timestamp = timestamp,
        totalSeconds = totalSeconds,
        outcomes = outcomes.map {
            SecondingSnapshotOutcome(
                outcomeId = it.outcomeId,
                percentage = it.percentage,
                seconds = it.seconds
            )
        }
    )
}

fun SecondingSnapshot.toDto(): SecondingSnapshotDto {
    return SecondingSnapshotDto(
        predictionId = predictionId,
        timestamp = timestamp,
        totalSeconds = totalSeconds,
        outcomes = outcomes.map {
            SecondingSnapshotOutcomeDto(
                outcomeId = it.outcomeId,
                percentage = it.percentage,
                seconds = it.seconds
            )
        }
    )
}

fun UserDto.toDomain(): Session.Authenticated {
    return Session.Authenticated(
        uid = uid,
        email = email,
        name = name,
        handle = handle,
        avatarUrl = avatarUrl,
        accuracy = accuracy,
        reputation = reputation,
        resolvedPredictionCount = resolvedPredictionCount
    )
}

fun Session.Authenticated.toDto(): UserDto {
    return UserDto(
        uid = uid,
        email = email,
        name = name,
        handle = handle,
        avatarUrl = avatarUrl,
        accuracy = accuracy,
        reputation = reputation,
        resolvedPredictionCount = resolvedPredictionCount
    )
}
