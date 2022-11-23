package dev.zygon.argus.location;

/**
 * Default available dimensions that a location can be tracked across. Can be
 * converted to the Minecraft equivalent through subtracting one from the
 * ordinal value.
 * <p>
 * Example conversion to Minecraft equivalent ID:
 * <p>
 * <code>
 *     var dimension = Dimension.OVERWORLD; <br/>
 *     var dimensionId = dimension.ordinal() - 1;
 * </code>
 * </p>
 * </p>
 */
public enum Dimension {

	NETHER,
	OVERWORLD,
	END
}
