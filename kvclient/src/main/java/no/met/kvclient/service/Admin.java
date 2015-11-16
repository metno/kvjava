package kvalobs.service;

public interface Admin {
	/**
	 * ping. In addition to be a ping it return false if some fatal error
	 * condition is detected in the pinged object. Use statusmessage to get more
	 * details of the error condition.
	 */
	boolean ping();

	/**
	 * @params details an integer that controls how much is information that is
	 *         returned in the message. As a guide use 0 (FATAL), 1 (ERROR), 2
	 *         (WARNING), 3 (INFO) 4 (DEBUG), etc. Higher number more
	 *         information.
	 */
	void statusmessage(short details, /* out */ String message);

	/**
	 * shutdown return true if it can shutdown false otherwise.
	 */
	boolean shutdown();
}
