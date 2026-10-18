import com.goggs.maneuver.physics.DamperMath;
import com.goggs.maneuver.physics.CeilingFlightMath;
import com.goggs.maneuver.physics.FieldBounds;
import com.goggs.maneuver.physics.FallDamageMath;
import com.goggs.maneuver.physics.GravityVector;

public final class PhysicsSelfTest {
    public static void main(String[] args) {
        FieldBounds defaults = FieldBounds.defaults();
        assertEquals(405.0d, defaults.volume(), 1.0e-9d, "default volume");
        assertTrue(defaults.contains(0, 0, 0, -4.0, -2.0, -4.0), "minimum boundary");
        assertTrue(!defaults.contains(0, 0, 0, 5.0, 0.0, 0.0), "maximum boundary");

        FieldBounds clamped = new FieldBounds(-999.0f, 999.0f, 1.0f, 0.0f, 999.0f, 3.0f);
        assertEquals(FieldBounds.MIN_OFFSET, clamped.offsetX(), 1.0e-9d, "minimum offset clamp");
        assertEquals(FieldBounds.MAX_OFFSET, clamped.offsetY(), 1.0e-9d, "maximum offset clamp");
        assertEquals(FieldBounds.MIN_SIZE, clamped.sizeX(), 1.0e-9d, "minimum size clamp");
        assertEquals(FieldBounds.MAX_SIZE, clamped.sizeY(), 1.0e-9d, "maximum size clamp");
        assertEquals(0.0625d, FieldBounds.MIN_SIZE, 1.0e-9d, "field size minimum");

        FieldBounds fractional = new FieldBounds(1.25f, -2.5f, 0.125f, 4.75f, 3.5f, 2.25f);
        assertEquals(1.25d, fractional.offsetX(), 1.0e-9d, "fractional offset");
        assertEquals(4.75d, fractional.sizeX(), 1.0e-9d, "fractional size");

        GravityVector normal = GravityVector.normal();
        assertEquals(0.0d, normal.deltaY(), 1.0e-9, "normal gravity delta");

        GravityVector zero = new GravityVector(0.0f, 0.0f, 0.0f);
        assertEquals(0.08d, zero.deltaY(), 1.0e-9, "zero-gravity cancels vanilla gravity");

        GravityVector up = new GravityVector(0.0f, 1.0f, 0.0f);
        assertEquals(0.16d, up.deltaY(), 1.0e-9, "upward gravity delta");

        GravityVector giant = new GravityVector(100.0f, 100.0f, 100.0f);
        double magnitude = Math.sqrt(
                giant.x() * giant.x() + giant.y() * giant.y() + giant.z() * giant.z()
        );
        assertTrue(magnitude <= GravityVector.MAX_MAGNITUDE + 1.0e-5, "gravity magnitude clamp");

        double near = DamperMath.velocityFactor(0.75d, 1.0d, 8.0d);
        double far = DamperMath.velocityFactor(0.75d, 7.0d, 8.0d);
        assertTrue(near < far && far < 1.0d, "radial damping falloff");
        assertTrue(DamperMath.velocityFactor(1.0d, 8.0d, 8.0d) == 1.0d, "outside radius is unchanged");

        double coefficient = DamperMath.coefficient(1.0d);
        double oneSecondFactor = Math.pow(DamperMath.velocityFactor(1.0d, 0.0d, 8.0d), 20.0d);
        assertEquals(Math.exp(-coefficient), oneSecondFactor, 1.0e-9, "exponential time law");

        assertEquals(0.0d, DamperMath.protectedFraction(0.0d, 4.0d), 1.0e-9, "no removed energy");
        assertEquals(0.5d, DamperMath.protectedFraction(4.0d, 4.0d), 1.0e-9, "half energy removed");

        double descentSpeed = -0.42d;
        for (int tick = 0; tick < 30; tick++) {
            descentSpeed = CeilingFlightMath.nextDescentVelocity(descentSpeed, 0.05d);
        }
        assertEquals(
                -0.225d,
                descentSpeed,
                0.0001d,
                "ceiling flight approaches creative-flight descent speed"
        );
        assertEquals(
                -0.225d,
                CeilingFlightMath.terminalDescentVelocity(0.05d),
                1.0e-9d,
                "creative-flight terminal descent speed"
        );

        assertEquals(1, FallDamageMath.vanillaFallDamage(4.0d, 1.0f, 3.0d, 1.0d), "vanilla fall damage");
        assertEquals(
                3.92d,
                FallDamageMath.terminalVelocity(0.08d),
                1.0e-9d,
                "vanilla terminal velocity"
        );
        double strongerGravityRatio = FallDamageMath.equivalentFallDistance(0.56d, 0.16d)
                / FallDamageMath.equivalentFallDistance(0.56d, 0.08d);
        assertTrue(
                strongerGravityRatio > 0.35d && strongerGravityRatio < 0.6d,
                "stronger gravity needs less fall distance for the same speed"
        );

        double speed = 0.0d;
        double distance = 0.0d;
        for (int tick = 0; tick < 10; tick++) {
            speed = (speed + 0.08d) * 0.98d;
            distance += speed;
        }
        assertEquals(
                distance,
                FallDamageMath.equivalentFallDistance(speed, 0.08d),
                0.08d,
                "discrete fall-distance inversion"
        );

        System.out.println("PhysicsSelfTest: all checks passed");
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertEquals(long expected, long actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double epsilon, String label) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}

